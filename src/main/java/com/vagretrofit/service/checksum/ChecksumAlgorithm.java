package com.vagretrofit.service.checksum;

/**
 * V2: Interface para algoritmos de checksum de EEPROMs automotivas.
 *
 * Arquitetura definida conforme requisito do usuário:
 * - Suporte a múltiplos checksums por dump
 * - Bloqueio de escrita se checksum não puder ser confirmado (zero-tolerance)
 * - Para IMMO2: sinalizar que validação requer FLASH do MCU HC08 (não apenas EEPROM)
 */
public interface ChecksumAlgorithm {

    /**
     * Nome do algoritmo para exibição e log.
     */
    String getAlgorithmName();

    /**
     * Localiza as posições de todos os bytes de checksum no dump.
     * @return array de offsets onde estão os bytes de checksum
     */
    int[] findChecksumLocations(byte[] data);

    /**
     * Verifica se todos os checksums do dump estão corretos.
     * @return resultado detalhado da validação
     */
    ChecksumResult validate(byte[] data);

    /**
     * Recalcula todos os checksums e retorna um novo array de bytes com os valores corrigidos.
     * @return novo array com checksums corrigidos
     * @throws UnsupportedOperationException se o algoritmo não suporta recálculo
     */
    byte[] recalculate(byte[] data) throws UnsupportedOperationException;

    /**
     * Indica se este algoritmo requer o arquivo FLASH do MCU para validação completa
     * (caso IMMO2 com MCU Motorola HC08).
     */
    boolean requiresMcuFlash();
}
