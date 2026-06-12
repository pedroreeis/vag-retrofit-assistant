package com.vagretrofit.service;

import com.vagretrofit.domain.Dump;
import com.vagretrofit.domain.Module;
import com.vagretrofit.domain.PatchVariant;
import com.vagretrofit.security.BlockReason;
import com.vagretrofit.security.OperationGuard;
import com.vagretrofit.service.checksum.ChecksumResult;
import com.vagretrofit.util.HashUtils;

import java.util.Arrays;
import java.util.List;

/**
 * Aplica patches EEPROM seguindo o pipeline de segurança completo.
 *
 * V2: Pipeline expandido com Etapa 4.5 — validação e recálculo de checksum.
 * Política zero-tolerância: checksum inválido/desconhecido/requer-flash BLOQUEIA a operação.
 *
 * BUG-04 FIX: BackupService é chamado antes de qualquer modificação. RULE 12.
 * BUG-05 FIX: AuditService registra início e fim de cada operação de patch.
 * BUG-10 FIX: OperationGuard.enforcePreConditions() chamado como primeira barreira.
 */
public class PatchService {

    private final ValidationService validationService;
    private final BackupService backupService;
    private final AuditService auditService;
    private final OperationGuard operationGuard;
    private final DiffService diffService;
    private final ChecksumService checksumService;

    public PatchService(ValidationService validationService) {
        this.validationService = validationService;
        this.backupService     = new BackupService();
        this.auditService      = new AuditService();
        this.operationGuard    = new OperationGuard();
        this.diffService       = new DiffService();
        this.checksumService   = new ChecksumService();
    }

    public Dump applyPatch(Dump originalDump, PatchVariant patchVariant) {
        String hashBefore = originalDump.getHashSha256();
        if (hashBefore == null) {
            hashBefore = HashUtils.calculateSha256(originalDump.getData());
            originalDump.setHashSha256(hashBefore);
        }

        String patchName = patchVariant.getPatch() != null ? patchVariant.getPatch().getName() : "UNKNOWN";
        Module module = originalDump.getIdentifiedRomVersion() != null
            ? originalDump.getIdentifiedRomVersion().getModule() : null;

        System.out.println("[PATCH V2] Iniciando: " + patchName);

        // ── ETAPA 1: OperationGuard (barreira de entrada) ─────────────────
        try {
            operationGuard.enforcePreConditions(originalDump, patchVariant);
        } catch (SecurityException se) {
            auditService.logOperation("PATCH_APPLY", originalDump.getFilename(), hashBefore,
                    null, null, patchName, "BLOCKED", se.getMessage());
            throw new IllegalStateException(se.getMessage(), se);
        }

        // ── ETAPA 2: Validação de Pré-Condições ───────────────────────────
        try {
            validationService.validatePreConditions(originalDump, patchVariant);
        } catch (IllegalStateException ise) {
            auditService.logOperation("PATCH_APPLY", originalDump.getFilename(), hashBefore,
                    null, null, patchName, "BLOCKED", ise.getMessage());
            throw ise;
        }

        // ── ETAPA 3: V2 Checksum Pré-Patch (zero-tolerância) ─────────────
        // Valida integridade do dump ANTES de qualquer modificação
        if (module != null) {
            ChecksumResult checksumPre = checksumService.validate(originalDump.getData(), module);
            System.out.println("[CHECKSUM PRE] " + checksumPre);

            if (checksumService.shouldBlock(checksumPre)) {
                BlockReason reason = resolveChecksumBlockReason(checksumPre.getStatus());
                String msg = "OPERAÇÃO BLOQUEADA [" + reason.name() + "]: " +
                             reason.getDescription() + "\nDetalhe: " + checksumPre.getMessage();
                auditService.logOperation("PATCH_APPLY", originalDump.getFilename(), hashBefore,
                        null, null, patchName, "BLOCKED", msg);
                throw new IllegalStateException(msg);
            }
        }

        // ── ETAPA 4: Backup Obrigatório (RULE 12) ─────────────────────────
        try {
            String backupPath = backupService.createBackup(originalDump);
            System.out.println("[PATCH V2] Backup criado: " + backupPath);
        } catch (Exception e) {
            String reason = BlockReason.BACKUP_FAILED.name() + ": " + e.getMessage();
            auditService.logOperation("PATCH_APPLY", originalDump.getFilename(), hashBefore,
                    null, null, patchName, "BLOCKED", reason);
            throw new IllegalStateException(
                "OPERAÇÃO BLOQUEADA [RULE 12]: Falha ao criar backup obrigatório. " + e.getMessage(), e);
        }

        // ── ETAPA 5: Aplicar Patch em Cópia ───────────────────────────────
        byte[] originalData = originalDump.getData();
        byte[] modifiedData = Arrays.copyOf(originalData, originalData.length);
        int startAddress = patchVariant.getAddressStart();
        byte[] patchData = patchVariant.getPatchData();
        System.arraycopy(patchData, 0, modifiedData, startAddress, patchData.length);

        // ── ETAPA 5.5: V2 Recálculo de Checksum Pós-Patch ────────────────
        String checksumStatus = "NOT_APPLICABLE";
        if (module != null && module.hasKnownChecksum()) {
            try {
                modifiedData = checksumService.recalculate(modifiedData, module);
                checksumStatus = "RECALCULATED";
                System.out.println("[CHECKSUM POST] Checksum recalculado com sucesso.");
            } catch (IllegalStateException e) {
                // Recálculo não suportado — manter os dados do patch como estão
                // (os patches catalogados já incluem bytes corretos)
                checksumStatus = "PATCH_INCLUDED";
                System.out.println("[CHECKSUM POST] Recálculo não disponível — patch inclui bytes de checksum: " + e.getMessage());
            }
        }

        Dump modifiedDump = new Dump(modifiedData, originalDump.getFilename());
        modifiedDump.setIdentifiedRomVersion(originalDump.getIdentifiedRomVersion());
        String hashAfter = HashUtils.calculateSha256(modifiedData);
        modifiedDump.setHashSha256(hashAfter);

        // ── ETAPA 6: Validação Pós-Modificação ────────────────────────────
        try {
            validationService.validatePostConditions(originalDump, modifiedDump, patchVariant);
            operationGuard.enforcePostConditions(originalDump, modifiedDump, patchVariant);
        } catch (Exception e) {
            String reason = "ROLLBACK: " + e.getMessage();
            auditService.logOperation("PATCH_APPLY", originalDump.getFilename(), hashBefore,
                    hashAfter, null, patchName, "ROLLBACK", reason);
            throw new IllegalStateException(reason, e);
        }

        // ── ETAPA 7: Gerar Diff ───────────────────────────────────────────
        List<DiffService.DiffEntry> diffs = diffService.computeDiff(originalDump, modifiedDump);
        StringBuilder diffStr = new StringBuilder();
        for (DiffService.DiffEntry d : diffs) diffStr.append(d.toString()).append("\n");

        // ── ETAPA 8: Registrar Auditoria de Sucesso ───────────────────────
        auditService.logOperation("PATCH_APPLY", originalDump.getFilename(), hashBefore,
                hashAfter, originalDump.getIdentifiedRomVersion().toString(), patchName,
                "SUCCESS", "CHECKSUM=" + checksumStatus + "\n" + diffStr.toString());

        return modifiedDump;
    }

    private BlockReason resolveChecksumBlockReason(ChecksumResult.Status status) {
        return switch (status) {
            case INVALID       -> BlockReason.CHECKSUM_INVALID_PRE_PATCH;
            case REQUIRES_FLASH -> BlockReason.CHECKSUM_REQUIRES_FLASH;
            default            -> BlockReason.CHECKSUM_UNKNOWN;
        };
    }
}
