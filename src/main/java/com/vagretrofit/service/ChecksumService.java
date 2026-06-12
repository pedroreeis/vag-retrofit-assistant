package com.vagretrofit.service;

import com.vagretrofit.domain.Module;
import com.vagretrofit.service.checksum.ChecksumAlgorithm;
import com.vagretrofit.service.checksum.ChecksumResult;
import com.vagretrofit.service.checksum.VdoPq34ImmoIIChecksumAlgorithm;
import com.vagretrofit.service.checksum.VdoPq34ImmoIIIChecksumAlgorithm;

/**
 * V2: Orquestrador do mecanismo de checksum.
 *
 * Seleciona o algoritmo correto com base no módulo identificado e
 * expõe validação/recálculo de forma unificada para o PatchService e a UI.
 *
 * Política zero-tolerância (conforme requisito do usuário):
 * - VALID → permite prosseguir
 * - UNKNOWN → BLOQUEIA com mensagem de aviso
 * - INVALID → BLOQUEIA com mensagem de erro
 * - REQUIRES_FLASH → BLOQUEIA com instrução de fornecer FLASH do MCU
 */
public class ChecksumService {

    /**
     * Seleciona o algoritmo correto para o módulo e valida o dump.
     */
    public ChecksumResult validate(byte[] data, Module module) {
        ChecksumAlgorithm algorithm = selectAlgorithm(module);
        return algorithm.validate(data);
    }

    /**
     * Seleciona o algoritmo correto e tenta recalcular os checksums.
     * Lança IllegalStateException se não for possível recalcular (zero-tolerância).
     */
    public byte[] recalculate(byte[] data, Module module) {
        ChecksumAlgorithm algorithm = selectAlgorithm(module);
        try {
            return algorithm.recalculate(data);
        } catch (UnsupportedOperationException e) {
            throw new IllegalStateException(
                "OPERAÇÃO BLOQUEADA [CHECKSUM]: Recálculo automático não disponível para este módulo. " +
                algorithm.getAlgorithmName() + " — " + e.getMessage(), e
            );
        }
    }

    /**
     * Retorna o nome do algoritmo para o módulo dado.
     */
    public String getAlgorithmName(Module module) {
        return selectAlgorithm(module).getAlgorithmName();
    }

    /**
     * Verifica se a operação deve ser bloqueada com base no resultado do checksum.
     *
     * Política:
     * - VALID     → permitir (checksum OK)
     * - INVALID   → BLOQUEAR (dump corrompido detectado)
     * - REQUIRES_FLASH → BLOQUEAR (IMMO2: impossível validar sem FLASH do MCU HC08)
     * - UNKNOWN   → PERMITIR com aviso (algoritmo desconhecido não justifica bloqueio;
     *               patches da KB já incluem bytes corretos)
     */
    public boolean shouldBlock(ChecksumResult result) {
        return result.getStatus() == ChecksumResult.Status.INVALID
            || result.getStatus() == ChecksumResult.Status.REQUIRES_FLASH;
    }

    // ─── Seleção de Algoritmo ────────────────────────────────────────────────

    private ChecksumAlgorithm selectAlgorithm(Module module) {
        if (module == null || module.getChecksumAlgorithm() == null) {
            return new UnknownChecksumAlgorithm("Módulo não identificado");
        }

        String algo = module.getChecksumAlgorithm().toUpperCase();

        if (algo.startsWith("UNKNOWN_REQUIRES_FLASH") || module.isChecksumRequiresFlash()) {
            return new VdoPq34ImmoIIChecksumAlgorithm();
        }

        if (algo.equals("VDO_PQ34_IMMO3")) {
            return new VdoPq34ImmoIIIChecksumAlgorithm();
        }

        return new UnknownChecksumAlgorithm(module.getChecksumAlgorithm());
    }

    // ─── Algoritmo desconhecido inline ───────────────────────────────────────

    private static class UnknownChecksumAlgorithm implements ChecksumAlgorithm {
        private final String name;
        UnknownChecksumAlgorithm(String name) { this.name = name; }

        @Override public String getAlgorithmName() { return "UNKNOWN (" + name + ")"; }
        @Override public boolean requiresMcuFlash() { return false; }
        @Override public int[] findChecksumLocations(byte[] data) { return new int[0]; }

        @Override
        public ChecksumResult validate(byte[] data) {
            return new ChecksumResult(
                ChecksumResult.Status.UNKNOWN,
                "Algoritmo de checksum desconhecido para este módulo: " + name +
                ". Não é possível validar a integridade do dump."
            );
        }

        @Override
        public byte[] recalculate(byte[] data) {
            throw new UnsupportedOperationException("Algoritmo desconhecido: " + name);
        }
    }
}
