package com.vagretrofit.service;

import com.vagretrofit.domain.Dump;
import com.vagretrofit.domain.PatchVariant;

/**
 * Valida pré e pós condições para aplicação de patches.
 * BUG-09 FIX: Comparação de ROM ID bytes feita diretamente no array,
 *             sem conversão para String (que causa problemas com chars acima de 0x7F).
 */
public class ValidationService {

    public void validatePreConditions(Dump dump, PatchVariant patchVariant) {
        // RULE 01: Tamanho do dump
        if (dump.getData() == null || dump.getData().length != 2048) {
            throw new IllegalStateException(
                "OPERAÇÃO BLOQUEADA [RULE 01]: Tamanho de dump inválido.");
        }

        // RULE 02: Dump deve ter sido identificado
        if (dump.getIdentifiedRomVersion() == null) {
            throw new IllegalStateException(
                "OPERAÇÃO BLOQUEADA [RULE 02]: Dump não identificado na KB.");
        }

        // RULE 03: ROM Version ID deve coincidir
        if (dump.getIdentifiedRomVersion().getId() != patchVariant.getTargetRomVersion().getId()) {
            throw new IllegalStateException(
                "OPERAÇÃO BLOQUEADA [RULE 03]: Patch incompatível com o ROM ID do dump. " +
                "Dump=" + dump.getIdentifiedRomVersion().getRomId() +
                " Patch_target=" + patchVariant.getTargetRomVersion().getRomId());
        }

        // RULE 04: Software Version deve coincidir
        String dumpSw = dump.getIdentifiedRomVersion().getModule().getSoftwareVersion();
        String patchSw = patchVariant.getSoftwareVersion();
        if (dumpSw == null || !dumpSw.equals(patchSw)) {
            throw new IllegalStateException(
                "OPERAÇÃO BLOQUEADA [RULE 04]: Incompatibilidade de versão de software. " +
                "Dump=" + dumpSw + " Patch=" + patchSw);
        }

        // RULE 05/06: IMMO2 vs IMMO3 — validação cruzada
        String dumpImmo = dump.getIdentifiedRomVersion().getModule().getImmo();
        String patchImmo = patchVariant.getTargetRomVersion() != null &&
                           patchVariant.getTargetRomVersion().getModule() != null
                           ? patchVariant.getTargetRomVersion().getModule().getImmo() : null;
        if (patchImmo != null) {
            if ("IMMO2".equals(dumpImmo) && "IMMO3".equals(patchImmo)) {
                throw new IllegalStateException(
                    "OPERAÇÃO BLOQUEADA [RULE 05]: Patch IMMO3 aplicado em módulo IMMO2.");
            }
            if ("IMMO3".equals(dumpImmo) && "IMMO2".equals(patchImmo)) {
                throw new IllegalStateException(
                    "OPERAÇÃO BLOQUEADA [RULE 06]: Patch IMMO2 aplicado em módulo IMMO3.");
            }
        }
    }

    public void validatePostConditions(Dump original, Dump modified, PatchVariant patchVariant) {
        // Tamanho deve ser idêntico
        if (original.getData().length != modified.getData().length) {
            throw new IllegalStateException(
                "ROLLBACK [POST-01]: Tamanho do dump foi alterado durante o patch. CRÍTICO.");
        }

        // Bytes do patch devem estar gravados corretamente
        int start = patchVariant.getAddressStart();
        byte[] patchData = patchVariant.getPatchData();
        for (int i = 0; i < patchData.length; i++) {
            if (modified.getData()[start + i] != patchData[i]) {
                throw new IllegalStateException(String.format(
                    "ROLLBACK [POST-02]: Byte em 0x%04X não foi gravado corretamente. " +
                    "Esperado=0x%02X Obtido=0x%02X",
                    start + i, patchData[i], modified.getData()[start + i]));
            }
        }

        // BUG-09 FIX: Verificar ROM ID como bytes diretamente, sem converter para String
        // Protege offset correto conforme a versão de software
        String dumpSw = original.getIdentifiedRomVersion() != null ? original.getIdentifiedRomVersion().getModule().getSoftwareVersion() : "";
        if ("VWK501MH".equals(dumpSw)) {
            if (!bytesEqual(original.getData(), modified.getData(), 0x4F8, 2)) {
                throw new IllegalStateException(
                    "ROLLBACK [POST-03]: Área protegida (ROM ID offset 0x4F8) foi alterada.");
            }
        } else if ("VWK503MH".equals(dumpSw)) {
            if (!bytesEqual(original.getData(), modified.getData(), 0x4FA, 2)) {
                throw new IllegalStateException(
                    "ROLLBACK [POST-04]: Área protegida (ROM ID offset 0x4FA) foi alterada.");
            }
        }
    }

    /**
     * BUG-09 FIX: Comparação de arrays de bytes sem conversão para String.
     */
    private boolean bytesEqual(byte[] a, byte[] b, int offset, int length) {
        for (int i = 0; i < length; i++) {
            if (a[offset + i] != b[offset + i]) return false;
        }
        return true;
    }
}
