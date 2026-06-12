package com.vagretrofit.security;

import com.vagretrofit.domain.Dump;
import com.vagretrofit.domain.PatchVariant;

/**
 * Gate keeper central de segurança — FAIL SAFE FIRST.
 * Implementa as 13 regras de bloqueio do plano de arquitetura (Seção 8.1).
 *
 * BUG-10 FIX: OperationGuard agora é chamado PRIMEIRO em PatchService,
 *             antes de ValidationService.
 * BUG-11 NOTE: Range 0x00-0x60 ainda hardcoded como área de proteção base;
 *              tabela EEPROM_MAP deve ser consultada quando populada.
 */
public class OperationGuard {

    public void enforcePreConditions(Dump dump, PatchVariant patch) {
        // RULE 01
        if (dump.getData() == null || dump.getData().length != 2048) {
            block(BlockReason.INVALID_DUMP_SIZE);
        }

        // RULE 02
        if (dump.getIdentifiedRomVersion() == null) {
            block(BlockReason.UNKNOWN_ROM_ID);
        }

        // RULE 03
        if (patch.getTargetRomVersion() == null ||
            dump.getIdentifiedRomVersion().getId() != patch.getTargetRomVersion().getId()) {
            block(BlockReason.PATCH_ROM_VERSION_MISMATCH);
        }

        // RULE 04
        String dumpSw = dump.getIdentifiedRomVersion().getModule().getSoftwareVersion();
        if (dumpSw == null || !dumpSw.equals(patch.getSoftwareVersion())) {
            block(BlockReason.SOFTWARE_VERSION_MISMATCH);
        }

        // RULE 05 & 06: IMMO cross-check
        String dumpImmo = dump.getIdentifiedRomVersion().getModule().getImmo();
        if (patch.getTargetRomVersion() != null && patch.getTargetRomVersion().getModule() != null) {
            String patchImmo = patch.getTargetRomVersion().getModule().getImmo();
            if ("IMMO2".equals(dumpImmo) && "IMMO3".equals(patchImmo)) {
                block(BlockReason.IMMO2_PATCH_ON_IMMO3_MODULE);
            }
            if ("IMMO3".equals(dumpImmo) && "IMMO2".equals(patchImmo)) {
                block(BlockReason.IMMO3_PATCH_ON_IMMO2_MODULE);
            }
        }

        // RULE 09: Part number desconhecido
        String partNumber = dump.getIdentifiedRomVersion().getModule().getPartNumber();
        if (partNumber == null || partNumber.contains("DESCONHECIDO")) {
            block(BlockReason.UNKNOWN_PART_NUMBER);
        }
    }

    public void enforcePostConditions(Dump original, Dump modified, PatchVariant patch) {
        // Tamanho deve ser idêntico
        if (original.getData().length != modified.getData().length) {
            block(BlockReason.INVALID_DUMP_SIZE);
        }

        // Área de coding base (0x00–0x60): hardcoded por ora (BUG-11: deve vir da EEPROM_MAP)
        // É seguro (FAIL SAFE) manter esse range como proteção conservadora.
        if (!isAreaIdentical(original.getData(), modified.getData(), 0x00, 0x60)) {
            block(BlockReason.PROTECTED_AREA_MODIFIED);
        }

        // ROM ID offsets — sempre protegidos, porém apenas para a versão correta
        String dumpSw = original.getIdentifiedRomVersion() != null ? original.getIdentifiedRomVersion().getModule().getSoftwareVersion() : "";
        if ("VWK501MH".equals(dumpSw)) {
            if (!isAreaIdentical(original.getData(), modified.getData(), 0x4F8, 0x4F9)) {
                block(BlockReason.PROTECTED_AREA_MODIFIED);
            }
        } else if ("VWK503MH".equals(dumpSw)) {
            if (!isAreaIdentical(original.getData(), modified.getData(), 0x4FA, 0x4FB)) {
                block(BlockReason.PROTECTED_AREA_MODIFIED);
            }
        }
    }

    private boolean isAreaIdentical(byte[] orig, byte[] mod, int start, int end) {
        for (int i = start; i <= end; i++) {
            if (orig[i] != mod[i]) return false;
        }
        return true;
    }

    private void block(BlockReason reason) {
        throw new SecurityException(
            "[FAIL SAFE ACTIVATED] " + reason.name() + ": " + reason.getDescription());
    }
}
