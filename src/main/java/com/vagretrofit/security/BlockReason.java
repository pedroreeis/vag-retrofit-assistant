package com.vagretrofit.security;

/**
 * Razões de bloqueio do OperationGuard.
 * BUG-15 FIX: Enum completado com todas as 13 regras do plano de arquitetura seção 8.1.
 */
public enum BlockReason {
    // RULE 01
    INVALID_DUMP_SIZE("dump.size ≠ expected_size. Arquivo binário não suportado ou truncado."),
    // RULE 02
    UNKNOWN_ROM_ID("rom_id NOT IN kb.rom_versions. ROM ID não encontrado na Base de Conhecimento."),
    // RULE 03
    PATCH_ROM_VERSION_MISMATCH("patch.rom_version_id ≠ dump.rom_id. Patch incompatível com o ROM ID do dump."),
    // RULE 04
    SOFTWARE_VERSION_MISMATCH("patch.software_version ≠ module.software_version. Incompatibilidade de versão de software."),
    // RULE 05
    IMMO2_PATCH_ON_IMMO3_MODULE("module.immo=IMMO2 AND patch.targets=IMMO3. Conflito IMMO2/IMMO3 detectado."),
    // RULE 06
    IMMO3_PATCH_ON_IMMO2_MODULE("module.immo=IMMO3 AND patch.targets=IMMO2. Conflito IMMO3/IMMO2 detectado."),
    // RULE 07
    PATCH_OFFSET_NOT_IN_KB("patch_offset NOT IN kb.eeprom_map. Offset do patch não catalogado na KB."),
    // RULE 08
    ORIGINAL_BYTES_MISMATCH("existing_bytes_at_offset ≠ expected_original. Bytes originais no offset diferem do esperado."),
    // RULE 09
    UNKNOWN_PART_NUMBER("module.part_number = DESCONHECIDO. Part number não identificado."),
    // RULE 10 — É WARNING, não bloqueia (não está aqui, tratado separadamente)
    // RULE 11
    KB_INTEGRITY_FAILED("kb.integrity_check FAILED. Integridade da Base de Conhecimento comprometida."),
    // RULE 12
    BACKUP_FAILED("backup FAILED. Falha ao criar backup obrigatório antes da modificação."),
    // RULE 13
    KNOWN_CONFLICT_UNACKNOWLEDGED("known_conflict EXISTS for operation. Conflito documentado não foi confirmado pelo usuário."),
    // Razão genérica para área protegida
    PROTECTED_AREA_MODIFIED("Tentativa de sobrescrita de área protegida (VIN/PIN/Odometer/ROM ID)."),
    // Razão genérica de plataforma
    UNSUPPORTED_PLATFORM("Plataforma ou painel não suportado pela Base de Conhecimento."),
    // V2: Checksum (zero-tolerância)
    CHECKSUM_INVALID_PRE_PATCH("Checksum do dump original está inválido. Dump possivelmente corrompido. Grave apenas a partir de um dump com checksum válido."),
    CHECKSUM_REQUIRES_FLASH("Módulo IMMO2: validação de checksum requer o arquivo FLASH do MCU Motorola HC08. Forneça o arquivo de FLASH para prosseguir."),
    CHECKSUM_UNKNOWN("Algoritmo de checksum desconhecido para este módulo. Não é possível confirmar integridade. Operação bloqueada por segurança."),
    CHECKSUM_RECALCULATION_FAILED("Falha ao recalcular checksum após aplicação do patch.");

    private final String description;

    BlockReason(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
