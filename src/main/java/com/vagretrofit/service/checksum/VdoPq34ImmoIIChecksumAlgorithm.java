package com.vagretrofit.service.checksum;

/**
 * V2: Algoritmo para módulos IMMO2 (ex: 1J0 920 825 A, V04).
 *
 * NOTA CRÍTICA (conforme especificação do usuário):
 * ─────────────────────────────────────────────────
 * Em painéis IMMO2 com MCU Motorola HC08, a lógica de checksum está
 * armazenada na MEMÓRIA FLASH do microcontrolador, não na EEPROM externa (24C02).
 * Portanto, a validação completa REQUER o arquivo binário da FLASH do MCU,
 * que não pode ser obtido apenas do dump da EEPROM.
 *
 * Comportamento desta implementação:
 * - validate(): retorna Status.REQUIRES_FLASH com mensagem explicativa
 * - recalculate(): lança UnsupportedOperationException
 * - requiresMcuFlash(): retorna TRUE
 *
 * O pipeline de segurança (OperationGuard + PatchService) trata REQUIRES_FLASH
 * como bloqueio zero-tolerância — não permite patch sem confirmação explícita.
 */
public class VdoPq34ImmoIIChecksumAlgorithm implements ChecksumAlgorithm {

    @Override
    public String getAlgorithmName() {
        return "VDO_PQ34_IMMO2 (Requer FLASH MCU HC08)";
    }

    @Override
    public boolean requiresMcuFlash() {
        return true; // IMMO2: checksum está na FLASH do MCU, não na EEPROM
    }

    @Override
    public int[] findChecksumLocations(byte[] data) {
        // Localização exata na FLASH desconhecida sem engenharia reversa do MCU HC08
        return new int[0];
    }

    @Override
    public ChecksumResult validate(byte[] data) {
        return new ChecksumResult(
            ChecksumResult.Status.REQUIRES_FLASH,
            "BLOQUEADO: Painel IMMO2 (MCU Motorola HC08). " +
            "A rotina de checksum está armazenada na FLASH do microcontrolador, " +
            "não na EEPROM externa. " +
            "Validação não é possível apenas com o dump da EEPROM. " +
            "Forneça o arquivo de FLASH do MCU HC08 para validação completa. " +
            "RISCO: Gravação sem validação pode resultar em brick, bootloop ou erro dEF."
        );
    }

    @Override
    public byte[] recalculate(byte[] data) {
        throw new UnsupportedOperationException(
            "Recálculo de checksum IMMO2 não disponível sem o firmware FLASH do MCU Motorola HC08. " +
            "Requer engenharia reversa do firmware para descobrir o algoritmo (CRC16, XOR, soma, etc.)."
        );
    }
}
