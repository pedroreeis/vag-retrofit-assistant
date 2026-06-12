package com.vagretrofit.service;

import com.vagretrofit.domain.Dump;
import com.vagretrofit.domain.RomVersion;
import com.vagretrofit.repository.KnowledgeBaseRepository;
import com.vagretrofit.util.HashUtils;
import com.vagretrofit.util.HexUtils;

import java.util.Optional;

/**
 * Identifica o módulo EEPROM carregado com base na KB.
 * BUG-07 FIX: Calcula SHA-256 do dump imediatamente após carregamento.
 * BUG-06 FIX: Registra operação de identificação na auditoria.
 */
public class IdentificationService {

    private final KnowledgeBaseRepository kbRepository;
    private final AuditService auditService;

    public IdentificationService() {
        this.kbRepository = new KnowledgeBaseRepository();
        this.auditService = new AuditService();
    }

    public void identify(Dump dump) {
        byte[] data = dump.getData();

        // BUG-07 FIX: Calcular hash SHA-256 imediatamente
        String hash = HashUtils.calculateSha256(data);
        dump.setHashSha256(hash);

        // FAIL SAFE RULE 01: Tamanho esperado para 24C02 = 2048 bytes
        if (data == null || data.length != 2048) {
            auditService.logOperation("IDENTIFY", dump.getFilename(), hash, null,
                    null, null, "BLOCKED", "INVALID_DUMP_SIZE");
            throw new IllegalStateException(
                "OPERAÇÃO BLOQUEADA [RULE 01]: Tamanho de dump inválido. " +
                "Esperado 2048 bytes (24C02), recebido: " + (data == null ? "null" : data.length));
        }

        // Extrair ROM IDs dos dois offsets possíveis
        // VWK501MH: 0x4F8-0x4F9 | VWK503MH: 0x4FA-0x4FB
        String romId501 = extractRomId(data, 0x4F8);
        String romId503 = extractRomId(data, 0x4FA);

        Optional<RomVersion> match = kbRepository.findRomVersionByRomId(romId501);
        if (match.isEmpty()) {
            match = kbRepository.findRomVersionByRomId(romId503);
        }

        if (match.isPresent()) {
            dump.setIdentifiedRomVersion(match.get());
            // BUG-06 FIX: Auditar identificação bem-sucedida
            auditService.logOperation("IDENTIFY", dump.getFilename(), hash, null,
                    match.get().toString(), null, "SUCCESS", null);
        } else {
            // FAIL SAFE RULE 02: ROM ID não na KB → BLOQUEAR
            auditService.logOperation("IDENTIFY", dump.getFilename(), hash, null,
                    null, null, "BLOCKED",
                    "UNKNOWN_ROM_ID [501_offset=" + romId501 + " 503_offset=" + romId503 + "]");
            throw new IllegalStateException(
                "OPERAÇÃO BLOQUEADA [RULE 02]: DESCONHECIDO. ROM ID não encontrado na Base de Conhecimento. " +
                "ROM IDs lidos: offset 0x4F8=" + romId501 + " | offset 0x4FA=" + romId503);
        }
    }

    private String extractRomId(byte[] data, int offset) {
        if (offset + 1 >= data.length) return "OFFSET_OOB";
        return HexUtils.bytesToHex(new byte[]{data[offset], data[offset + 1]});
    }
}
