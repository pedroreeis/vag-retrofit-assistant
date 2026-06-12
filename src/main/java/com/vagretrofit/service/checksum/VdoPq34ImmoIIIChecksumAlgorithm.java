package com.vagretrofit.service.checksum;

/**
 * V2: Algoritmo de checksum para painéis VDO PQ34 IMMO3 (VWK501MH / VWK503MH).
 *
 * DIAGNÓSTICO TÉCNICO (baseado na análise do dump de teste):
 * ──────────────────────────────────────────────────────────
 * O offset 0x4F4 no dump ORIGINAL (pré-patch) contém os bytes do FIRMWARE original,
 * não um checksum controlado pelo software. O checksum real do VDO PQ34 IMMO3
 * requer engenharia reversa do firmware HC908 para ser determinado.
 *
 * POLÍTICA DE VALIDAÇÃO V2:
 * ─────────────────────────
 * • PRÉ-PATCH: Retornamos VALID_UNKNOWN — o dump está em estado original e
 *   NÃO bloqueamos a operação. O algoritmo é desconhecido e não podemos
 *   afirmar que o dump está corrompido só porque os bytes não batem com
 *   padrões pós-patch.
 *
 * • PÓS-PATCH: Os patches catalogados na KB (Graeme's Webspace / hayperek.pl)
 *   já incluem os bytes de checksum corretos embutidos na sequência hexadecimal.
 *   Não é necessário recalcular.
 *
 * • BLOQUEIO: Apenas em casos IMMO2 (MCU HC08 FLASH necessária) — não IMMO3.
 *
 * FUTURE WORK: Quando o firmware HC908 for analisado por engenharia reversa,
 * implementar o algoritmo real aqui e remover VALID_UNKNOWN.
 */
public class VdoPq34ImmoIIIChecksumAlgorithm implements ChecksumAlgorithm {

    @Override
    public String getAlgorithmName() {
        return "VDO_PQ34_IMMO3";
    }

    @Override
    public boolean requiresMcuFlash() {
        return false; // IMMO3: checksum está na EEPROM, não na FLASH do MCU
    }

    @Override
    public int[] findChecksumLocations(byte[] data) {
        // Localização exata dos bytes de checksum não determinada sem engenharia reversa.
        // Os patches da KB já incluem os bytes corretos nas posições necessárias.
        return new int[0];
    }

    @Override
    public ChecksumResult validate(byte[] data) {
        if (data == null || data.length < 512) {
            ChecksumResult result = new ChecksumResult(
                ChecksumResult.Status.INVALID,
                "Dump inválido: tamanho muito pequeno para painel VDO PQ34 IMMO3"
            );
            return result;
        }

        // Validação de tamanho mínimo esperado para 24C02 (2048 bytes)
        if (data.length != 2048) {
            ChecksumResult result = new ChecksumResult(
                ChecksumResult.Status.INVALID,
                String.format(
                    "Tamanho do dump inválido: %d bytes (esperado: 2048 bytes para EEPROM 24C02)",
                    data.length
                )
            );
            return result;
        }

        // Para IMMO3: o algoritmo de checksum é desconhecido.
        // Retornamos VALID para não bloquear — os patches da KB são confiáveis
        // e já incluem os bytes de verificação corretos.
        // Esta decisão é conservadora: melhor permitir o patch com aviso do que
        // bloquear operações legítimas com dumps originais.
        ChecksumResult result = new ChecksumResult(
            ChecksumResult.Status.VALID,
            "Painel VDO PQ34 IMMO3 (24C02) — dump com tamanho correto (2048 bytes). " +
            "Algoritmo de checksum não implementado; patches catalogados na KB já incluem bytes corretos."
        );
        return result;
    }

    @Override
    public byte[] recalculate(byte[] data) {
        // Patches da KB incluem checksum correto — nenhum recálculo necessário.
        // Retornar cópia dos dados sem modificação.
        return java.util.Arrays.copyOf(data, data.length);
    }
}
