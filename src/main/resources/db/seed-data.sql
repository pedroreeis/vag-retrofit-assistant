-- VAG Retrofit Assistant - Seed Data v1.0.0
-- Inserções baseadas EXCLUSIVAMENTE na Base de Conhecimento Mestre
-- BUG-13 FIX: Part numbers completos conforme plano seção 9.4

INSERT INTO KB_VERSION (version, schema_version, release_date, changelog, hash_sha256)
VALUES ('2.0.0', '2.0', datetime('now'), 'V2: multi-module, checksum engine, K-Line kw1281test integration', 'PENDING');

-- ============================================================
-- PLATFORMS
-- ============================================================
INSERT INTO PLATFORM (code, name, bus_type, vehicles, notes, source_document, source_version, source_date)
VALUES ('PQ34', 'Golf Mk4 / Bora / Jetta', 'K-Line+CAN',
        'Golf Mk4, Bora, Jetta Mk4, Leon 1M, Octavia 1U, A3 8L',
        'CAN limitado. Gateways customizados podem ter comportamento variável.',
        'Retrofit Knowledge Base', '1.0', '2026-06-10');

INSERT INTO PLATFORM (code, name, bus_type, vehicles, notes, source_document, source_version, source_date)
VALUES ('PQ35', 'Golf Mk5 / Jetta Mk5', 'Full CAN',
        'Golf Mk5, Jetta Mk5, Leon 1P, Octavia 1Z, A3 8P',
        'CAN completo. Cluster Mk5 não catalogado na KB Mestre.',
        'Retrofit Knowledge Base', '1.0', '2026-06-10');

-- ============================================================
-- MODULE TYPES
-- ============================================================
INSERT INTO MODULE_TYPE (manufacturer, category) VALUES ('VDO', 'Cluster');
INSERT INTO MODULE_TYPE (manufacturer, category) VALUES ('Motometer', 'Cluster');
INSERT INTO MODULE_TYPE (manufacturer, category) VALUES ('Magneti Marelli', 'Cluster');

-- ============================================================
-- MODULES — Part Numbers Catalogados (Fonte: Graeme's Webspace)
-- BUG-13 FIX: Todos os 10 part numbers do plano seção 9.4 inseridos
-- ============================================================

-- VWK501MH Clusters (.:R32) — IMMO3
INSERT INTO MODULE (platform_id, module_type_id, part_number, software_version, eeprom_type, eeprom_size_bytes, immo, features, module_address, checksum_algorithm, checksum_requires_flash, source_document, source_version, source_date)
VALUES (1, 1, '1J0920826J', 'VWK501MH', '24C02', 2048, 'IMMO3', 'Metric MFA, Needle Sweep', '17', 'VDO_PQ34_IMMO3', 0, 'Graeme''s Webspace', 'Unknown', 'Unknown');

-- VWK503MH Clusters (.:R32 DSG) — IMMO3
INSERT INTO MODULE (platform_id, module_type_id, part_number, software_version, eeprom_type, eeprom_size_bytes, immo, features, module_address, checksum_algorithm, checksum_requires_flash, source_document, source_version, source_date)
VALUES (1, 1, '1J0920826K', 'VWK503MH', '24C02', 2048, 'IMMO3', 'Metric MFA DSG, Needle Sweep', '17', 'VDO_PQ34_IMMO3', 0, 'Graeme''s Webspace', 'Unknown', 'Unknown');

-- VWK501MH FIS Cluster — IMMO3
INSERT INTO MODULE (platform_id, module_type_id, part_number, software_version, eeprom_type, eeprom_size_bytes, immo, features, module_address, checksum_algorithm, checksum_requires_flash, source_document, source_version, source_date)
VALUES (1, 1, '1J0920846F', 'VWK501MH', '24C02', 2048, 'IMMO3', 'Metric FIS, Needle Sweep, Welcome Message', '17', 'VDO_PQ34_IMMO3', 0, 'Graeme''s Webspace', 'Unknown', 'Unknown');

-- VWK503MH FIS DSG — IMMO3
INSERT INTO MODULE (platform_id, module_type_id, part_number, software_version, eeprom_type, eeprom_size_bytes, immo, features, module_address, checksum_algorithm, checksum_requires_flash, source_document, source_version, source_date)
VALUES (1, 1, '1J0920846G', 'VWK503MH', '24C02', 2048, 'IMMO3', 'Metric FIS DSG, Needle Sweep', '17', 'VDO_PQ34_IMMO3', 0, 'Graeme''s Webspace', 'Unknown', 'Unknown');

-- VWK501MH UK MFA — IMMO3
INSERT INTO MODULE (platform_id, module_type_id, part_number, software_version, eeprom_type, eeprom_size_bytes, immo, features, module_address, checksum_algorithm, checksum_requires_flash, source_document, source_version, source_date)
VALUES (1, 1, '1J0920926J', 'VWK501MH', '24C02', 2048, 'IMMO3', 'UK MFA, Needle Sweep', '17', 'VDO_PQ34_IMMO3', 0, 'Graeme''s Webspace', 'Unknown', 'Unknown');

-- VWK503MH US MFA — IMMO3
INSERT INTO MODULE (platform_id, module_type_id, part_number, software_version, eeprom_type, eeprom_size_bytes, immo, features, module_address, checksum_algorithm, checksum_requires_flash, source_document, source_version, source_date)
VALUES (1, 1, '1J0920927A', 'VWK503MH', '24C02', 2048, 'IMMO3', 'US MFA, Needle Sweep', '17', 'VDO_PQ34_IMMO3', 0, 'Graeme''s Webspace', 'Unknown', 'Unknown');

-- VWK501MH UK FIS — IMMO3
INSERT INTO MODULE (platform_id, module_type_id, part_number, software_version, eeprom_type, eeprom_size_bytes, immo, features, module_address, checksum_algorithm, checksum_requires_flash, source_document, source_version, source_date)
VALUES (1, 1, '1J0920946F', 'VWK501MH', '24C02', 2048, 'IMMO3', 'UK FIS, Needle Sweep', '17', 'VDO_PQ34_IMMO3', 0, 'Graeme''s Webspace', 'Unknown', 'Unknown');

-- VDO IMMO2 — V04
INSERT INTO MODULE (platform_id, module_type_id, part_number, sw_version_code, eeprom_type, eeprom_size_bytes, immo, module_address, checksum_algorithm, checksum_requires_flash, source_document, source_version, source_date)
VALUES (1, 1, '1J0 920 825 A', 'V04', '24C02', 2048, 'IMMO2', '17', 'UNKNOWN_REQUIRES_FLASH', 1, 'Retrofit Knowledge Base', '1.0', '2026-06-10');

-- VDO IMMO3 — V01 (EEPROM type DESCONHECIDO)
INSERT INTO MODULE (platform_id, module_type_id, part_number, sw_version_code, eeprom_type, eeprom_size_bytes, immo, module_address, checksum_algorithm, checksum_requires_flash, source_document, source_version, source_date)
VALUES (1, 1, '1JE 920 846 F', 'V01', 'DESCONHECIDO', 2048, 'IMMO3', '17', 'UNKNOWN', 0, 'Retrofit Knowledge Base', '1.0', '2026-06-10');

-- VDO IMMO2 básico — V07
INSERT INTO MODULE (platform_id, module_type_id, part_number, sw_version_code, eeprom_type, eeprom_size_bytes, immo, module_address, checksum_algorithm, checksum_requires_flash, source_document, source_version, source_date)
VALUES (1, 1, '1J0 920 806 C', 'V07', '24C02', 2048, 'IMMO2', '17', 'UNKNOWN_REQUIRES_FLASH', 1, 'Retrofit Knowledge Base', '1.0', '2026-06-10');

-- ============================================================
-- V2: Confort Module (endereço 46) — placeholder para operador expandir
-- ============================================================
INSERT INTO MODULE (platform_id, module_type_id, part_number, software_version, eeprom_type, eeprom_size_bytes, immo, module_address, checksum_algorithm, checksum_requires_flash, features, limitations, source_document, source_version, source_date)
VALUES (1, 1, '1J0959799', 'DESCONHECIDO', 'DESCONHECIDO', 0, 'N/A', '46', 'UNKNOWN', 0,
        'Window close with lock, window open with unlock, auto lock',
        'Patches não catalogados na KB — operador deve adicionar manualmente',
        'Retrofit Knowledge Base', '2.0', '2026-06-11');

-- ============================================================
-- ROM VERSIONS
-- VWK501MH: module_id=1 (1J0920826J) como representante do software
-- BUG-14 NOTE: ROM IDs pertencem ao software version VWK501MH —
--              múltiplos módulos compartilham o mesmo SW.
--              Para V2, o schema deverá normalizar isso com uma tabela
--              SOFTWARE_VERSION independente do MODULE.
-- ============================================================

-- ROM ID 00A4 (SW 01.00) — VWK501MH
INSERT INTO ROM_VERSION (module_id, rom_id, sticker_code, rom_id_offset, source_document, source_version, source_date)
VALUES (1, '00A4', '01.00', '0x4F8-0x4F9', 'Graeme''s Webspace', 'Unknown', 'Unknown');

-- ROM ID 10A4 (SW 01.10) — VWK501MH
INSERT INTO ROM_VERSION (module_id, rom_id, sticker_code, rom_id_offset, source_document, source_version, source_date)
VALUES (1, '10A4', '01.10', '0x4F8-0x4F9', 'Graeme''s Webspace', 'Unknown', 'Unknown');

-- ROM ID 92A3 (SW 00.92) — VWK501MH
INSERT INTO ROM_VERSION (module_id, rom_id, sticker_code, rom_id_offset, source_document, source_version, source_date)
VALUES (1, '92A3', '00.92', '0x4F8-0x4F9', 'Graeme''s Webspace', 'Unknown', 'Unknown');

-- ROM ID 00AE (SW 09.00) — VWK503MH (module_id=2)
INSERT INTO ROM_VERSION (module_id, rom_id, sticker_code, rom_id_offset, source_document, source_version, source_date)
VALUES (2, '00AE', '09.00', '0x4FA-0x4FB', 'Graeme''s Webspace', 'Unknown', 'Unknown');

-- ============================================================
-- EEPROM_MAP — Regiões catalogadas (Fonte: plano de arquitetura seção 8.3)
-- BUG-11: Populando EEPROM_MAP para que OperationGuard possa ler da KB
-- ============================================================
INSERT INTO EEPROM_MAP (module_id, address_start, address_end, region_name, description, data_type, source_document, source_version, source_date)
VALUES (1, '0x0000', '0x00FF', 'Coding e Configuração IMMO2', 'Área de coding, somente patches catalogados', 'Coding', 'Plano de Arquitetura V1', '1.0', '2026-06-10');

INSERT INTO EEPROM_MAP (module_id, address_start, address_end, region_name, description, data_type, source_document, source_version, source_date)
VALUES (1, '0x4F8', '0x4F9', 'ROM ID VWK501MH', 'SOMENTE LEITURA — identificador de versão de ROM', 'ROM_ID', 'Graeme''s Webspace', 'Unknown', 'Unknown');

INSERT INTO EEPROM_MAP (module_id, address_start, address_end, region_name, description, data_type, source_document, source_version, source_date)
VALUES (2, '0x4FA', '0x4FB', 'ROM ID VWK503MH', 'SOMENTE LEITURA — identificador de versão de ROM', 'ROM_ID', 'Graeme''s Webspace', 'Unknown', 'Unknown');

INSERT INTO EEPROM_MAP (module_id, address_start, address_end, region_name, description, data_type, source_document, source_version, source_date)
VALUES (1, '0x4F4', '0x693', 'Área de Patch Needle Sweep 501MH 00A4', 'Área modificável pelo patch Needle Sweep Bodie', 'Patch', 'Graeme''s Webspace', 'Unknown', 'Unknown');

INSERT INTO EEPROM_MAP (module_id, address_start, address_end, region_name, description, data_type, source_document, source_version, source_date)
VALUES (2, '0x4F6', '0x6B9', 'Área de Patch Needle Sweep 503MH 00AE', 'Área modificável pelo patch Needle Sweep Bodie', 'Patch', 'Graeme''s Webspace', 'Unknown', 'Unknown');

-- ============================================================
-- PATCHES
-- ============================================================
INSERT INTO PATCH (name, description, category, author, license)
VALUES ('Needle Sweep Bodie', 'Ativação do Staging/Needle Sweep — animação de ponteiros na ignição', 'Needle Sweep', 'Bodie', 'Unspecified');

INSERT INTO PATCH (name, description, category, author, license)
VALUES ('Led FullSweep hayperek', 'Needle Sweep estendido com LEDs de combustível e temperatura + animação', 'Led FullSweep', 'hayperek', 'LGPLv3');

-- ============================================================
-- PATCH VARIANTS
-- BUG-12 NOTE: patch_data contém X'00' como placeholder.
-- Os dados hexadecimais reais precisam ser extraídos dos arquivos
-- Graeme''s patchs 501.html e hayperek.pl patchs.html.
-- patch_data_hex documenta o status de placeholder para auditoria.
-- ============================================================

-- Graeme — 00A4 (rom_version_id=1)
INSERT INTO PATCH_VARIANT (patch_id, rom_version_id, software_version, address_start, address_end, patch_data, patch_data_hex, cluster_level, preconditions, safeguards, source_document, source_version, source_date)
VALUES (1, 1, 'VWK501MH', '0x4F4', '0x693', X'F01A800100A400000000000000000000000AA40285020A0A650284024C2485000000000000000000000000000000000000000000000000000000000000000000000000000000F0000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000AD36082907C9059002A9040A4C18A500000000000000000000000000000000000000000000000000005AACB80CA58AC90EF0069CF80F4CF30FADF70FF009E00ED0F49CF70F80EFADF80FD019C003B0E6E006F00AE008F00DE01EF009805FA2258E2D0BA21EA9018DF80FC0039018C00EB014A9CF8D6E02A9F18D7002A90F8D71028D6F028010C01BB0339C6E029C6F029C70029C7102ADB0168D7202ADB1169C7302AD20178D6C02AD21178D6D02A9021481A9201C2C0B7A68A95D485A7A7C5F5601000001851C4C01851E0E00A5124C00A5140F0057494C00574B0F007F', 'EXTRACTED', 'FIS/MFA',
        'Midline ou Highline. NÃO Lowline.',
        'Checksum DESCONHECIDO. Risco de erro dEF.',
        'Graeme''s Webspace', 'Unknown', 'Unknown');

-- Graeme — 10A4 (rom_version_id=2)
INSERT INTO PATCH_VARIANT (patch_id, rom_version_id, software_version, address_start, address_end, patch_data, patch_data_hex, cluster_level, preconditions, safeguards, source_document, source_version, source_date)
VALUES (1, 2, 'VWK501MH', '0x4F4', '0x691', X'161A7E0110A40000000000000000000AA40285020A0A650284024C2485000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000AD38082907C9059002A9040A4C18A50000000000000000000000000000000000000000000000005AACBA0CA58AC90EF0069CF80F4CF30FADF70FF009E00ED0F49CF70F80EFADF80FD019C003B0E6E006F00AE008F00DE01EF009805FA2258E2F0BA21EA9018DF80FC0039018C00EB014A9CF8D6E02A9F18D7002A90F8D71028D6F028010C01BB0339C6E029C6F029C70029C7102ADB0168D7202ADB1169C7302AD20178D6C02AD21178D6D02A9021481A9201C2E0B7A68A95D485A7A7C5F5601000001851C4C01851E0E00A5124C00A5140F0057494C00574B0F007F', 'EXTRACTED', 'FIS/MFA',
        'Midline ou Highline. NÃO Lowline.',
        'Checksum DESCONHECIDO. Risco de erro dEF.',
        'Graeme''s Webspace', 'Unknown', 'Unknown');

-- Graeme — 92A3 (rom_version_id=3)
INSERT INTO PATCH_VARIANT (patch_id, rom_version_id, software_version, address_start, address_end, patch_data, patch_data_hex, cluster_level, preconditions, safeguards, source_document, source_version, source_date)
VALUES (1, 3, 'VWK501MH', '0x4F4', '0x691', X'971A7E0192A30000000000000000000AA40285020A0A650284024C2485000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000AD38082907C9059002A9040A4C18A50000000000000000000000000000000000000000000000005AACBA0CA58AC90EF0069CF80F4CF30FADF70FF009E00ED0F49CF70F80EFADF80FD019C003B0E6E006F00AE008F00DE01EF009805FA2258E2F0BA21EA9018DF80FC0039018C00EB014A9CF8D6E02A9F18D7002A90F8D71028D6F028010C01BB0339C6E029C6F029C70029C7102ADB0168D7202ADB1169C7302AD20178D6C02AD21178D6D02A9021481A9201C2E0B7A68A95D485A7A7C5F5601000001851C4C01851E0E00A5124C00A5140F0057494C00574B0F007F', 'EXTRACTED', 'FIS/MFA',
        'Midline ou Highline. NÃO Lowline.',
        'Checksum DESCONHECIDO. Risco de erro dEF.',
        'Graeme''s Webspace', 'Unknown', 'Unknown');

-- Hayperek — 92A3 (rom_version_id=3)
INSERT INTO PATCH_VARIANT (patch_id, rom_version_id, software_version, address_start, address_end, patch_data, patch_data_hex, cluster_level, preconditions, safeguards, source_document, source_version, source_date)
VALUES (2, 3, 'VWK501MH', '0x4F4', '0x675', X'2A16660192A35AACBA0CA58AC90EF0069C0E0F4C090FAD0D0FF009E00ED0F49C0D0F80EFAD0E0FD019C003B0E6E006F00AE008F00DE01EF0098058A2258E2F0BA21EA9018D0E0FC0039026C00EB022A9CF8D6E02A9F18D7002A90F8D71028D6F02A9058D73029C72028D6D029C6C02801CC01BB01E9C6E029C6F029C70029C71029C73029C72029C6D029C6C027A68A95D485A7A7C5F560100AD0D0FD0326403640264019CC10A9CC20AACBA0CC020B01FC0189002A000B9760F8DC10AB9920F8DC20AA54325FE19AE0F8D861F4C01C84CE2C60000000000000000000000005B70617463680062795D0000000000005B686179706572656B5D0000000000000000000000010103030303070707070703030303010101000000000000000000008081C1C5C7E7EFEFEFEFEFE7C7C5C18180800000000000000000000000000000000101010101010100000000000000000000000000000000000000000100000000000000000000000000000001452A1601452B0F0057494C00574A7C00574B0E007F', 'EXTRACTED', 'FIS/MFA',
        'Midline ou Highline. NÃO Lowline.',
        'Checksum DESCONHECIDO. Risco de erro dEF. Licença LGPLv3.',
        'hayperek.pl', 'Unknown', 'Unknown');

-- Hayperek — 00A4 (rom_version_id=1)
INSERT INTO PATCH_VARIANT (patch_id, rom_version_id, software_version, address_start, address_end, patch_data, patch_data_hex, cluster_level, preconditions, safeguards, source_document, source_version, source_date)
VALUES (2, 1, 'VWK501MH', '0x4F4', '0x665', X'FB16560100A45AACB80CA58AC90EF0069C0C0F4C070FAD0B0FF009E00ED0F49C0B0F80EFAD0C0FD019C003B0E6E006F00AE008F00DE01EF0098058A2258E2D0BA21EA9018D0C0FC0039026C00EB022A9CF8D6E02A9F18D7002A90F8D71028D6F02A9058D73029C72028D6D029C6C02801CC01BB01E9C6E029C6F029C70029C71029C73029C72029C6D029C6C027A68A95D485A7A7C5F560100AD0B0FD0326403640264019CBF0A9CC00AACB80CC020B01FC0189002A000B9740F8DBF0AB9900F8DC00AA54325FE19AC0F8D861F4C68C74C49C60000000000000000000000005B70617463680062795D0000000000005B686179706572656B5D0000000000000000000000010103030303070707070703030303010101000000000000000000008081C1C5C7E7EFEFEFEFEFE7C7C5C18180800000000000000000000000000000000101010101010100000000000000000000000000000000000000000057494C00574A7A00574B0E014491140144920F007F', 'EXTRACTED', 'FIS/MFA',
        'Midline ou Highline. NÃO Lowline.',
        'Checksum DESCONHECIDO. Risco de erro dEF. Licença LGPLv3.',
        'hayperek.pl', 'Unknown', 'Unknown');

-- Hayperek — 10A4 (rom_version_id=2)
INSERT INTO PATCH_VARIANT (patch_id, rom_version_id, software_version, address_start, address_end, patch_data, patch_data_hex, cluster_level, preconditions, safeguards, source_document, source_version, source_date)
VALUES (2, 2, 'VWK501MH', '0x4F4', '0x675', X'A916660110A45AACBA0CA58AC90EF0069C0E0F4C090FAD0D0FF009E00ED0F49C0D0F80EFAD0E0FD019C003B0E6E006F00AE008F00DE01EF0098058A2258E2F0BA21EA9018D0E0FC0039026C00EB022A9CF8D6E02A9F18D7002A90F8D71028D6F02A9058D73029C72028D6D029C6C02801CC01BB01E9C6E029C6F029C70029C71029C73029C72029C6D029C6C027A68A95D485A7A7C5F560100AD0D0FD0326403640264019CC10A9CC20AACBA0CC020B01FC0189002A000B9760F8DC10AB9920F8DC20AA54325FE19AE0F8D861F4C01C84CE2C60000000000000000000000005B70617463680062795D0000000000005B686179706572656B5D0000000000000000000000010103030303070707070703030303010101000000000000000000008081C1C5C7E7EFEFEFEFEFE7C7C5C18180800000000000000000000000000000000101010101010100000000000000000000000000000000000000000100000000000000000000000000000001452A1601452B0F0057494C00574A7C00574B0E007F', 'EXTRACTED', 'FIS/MFA',
        'Midline ou Highline. NÃO Lowline.',
        'Checksum DESCONHECIDO. Risco de erro dEF. Licença LGPLv3.',
        'hayperek.pl', 'Unknown', 'Unknown');

-- Graeme — 00AE (rom_version_id=4)
INSERT INTO PATCH_VARIANT (patch_id, rom_version_id, software_version, address_start, address_end, patch_data, patch_data_hex, cluster_level, preconditions, safeguards, source_document, source_version, source_date)
VALUES (1, 4, 'VWK503MH', '0x4F6', '0x6B9', X'561AA40100AE5AACC10CA58AC90EF0069C200F4C1B0FAD1F0FF009E00ED0F49C1F0F80EFAD200FD019C003B0E6E006F00AE008F00DE01EF0098063A2258E370BA21EA9018D200FC0039018C00EB014A9CF8D7002A9F18D7202A90F8D73028D71028010C01BB0379C70029C71029C72029C7302ADA8168D7402ADA9169C7502AD08178D6E02AD09178D6F02A9021481A9201C360B7A6868A95648A90D485A7A7C0F55010000000000000000000000000000000000000000000000000020392CF0188A293FC90CF004C90BD00DA923CDD515B0068DD51520F48AA9006000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000A9402C0F16F00FA9082436F009AD131629F0C930D0034C65E7A9012CF60AF001604C0FE7FFFFFFFFFFFFFFFFFFFF0055F94C0055FA830055FB0E0166860F0106530F0136E90F007F', 'EXTRACTED', 'FIS/MFA',
        'Midline ou Highline. NÃO Lowline.',
        'Checksum DESCONHECIDO. Risco de erro dEF.',
        'Graeme''s Webspace', 'Unknown', 'Unknown');

