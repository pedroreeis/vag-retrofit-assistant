-- VAG Retrofit Assistant - Knowledge Base Schema v2.0.0
-- FAIL SAFE FIRST

CREATE TABLE PLATFORM (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    code TEXT NOT NULL, -- PQ34, PQ35
    name TEXT NOT NULL,
    bus_type TEXT,
    vehicles TEXT,
    notes TEXT,
    source_document TEXT NOT NULL,
    source_version TEXT,
    source_date TEXT
);

CREATE TABLE MODULE_TYPE (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    manufacturer TEXT NOT NULL, -- VDO, Motometer, Magneti Marelli
    category TEXT NOT NULL -- Cluster, ECU, CCM
);

CREATE TABLE MODULE (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    platform_id INTEGER NOT NULL,
    module_type_id INTEGER NOT NULL,
    part_number TEXT NOT NULL,
    software_version TEXT,
    sw_version_code TEXT,
    eeprom_type TEXT,
    eeprom_size_bytes INTEGER,
    immo TEXT,
    features TEXT,
    limitations TEXT,
    -- V2: address do módulo no barramento VAG (17=Instruments, 46=Comfort, etc.)
    module_address TEXT DEFAULT '17',
    -- V2: informações de checksum
    checksum_algorithm TEXT,           -- 'VDO_PQ34_IMMO3', 'VDO_PQ34_IMMO2_FLASH', 'UNKNOWN', etc.
    checksum_count INTEGER DEFAULT 1,  -- quantidade de checksums no dump
    -- Para IMMO2: caminho/tipo do arquivo FLASH do MCU HC08 necessário para validação
    checksum_requires_flash INTEGER DEFAULT 0, -- 0=false, 1=true
    source_document TEXT NOT NULL,
    source_version TEXT,
    source_date TEXT,
    FOREIGN KEY (platform_id) REFERENCES PLATFORM(id),
    FOREIGN KEY (module_type_id) REFERENCES MODULE_TYPE(id)
);

-- V2: Tabela normalizada de versões de software (BUG-14 fix)
-- ROM IDs pertencem ao software, não ao módulo físico
CREATE TABLE SOFTWARE_VERSION (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    code TEXT NOT NULL UNIQUE,         -- VWK501MH, VWK503MH
    description TEXT,
    compatible_module_types TEXT       -- lista de tipos de módulo compatíveis
);

CREATE TABLE ROM_VERSION (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    module_id INTEGER NOT NULL,
    rom_id TEXT NOT NULL,
    sticker_code TEXT,
    rom_id_offset TEXT,
    compatible_vehicles TEXT,
    source_document TEXT NOT NULL,
    source_version TEXT,
    source_date TEXT,
    FOREIGN KEY (module_id) REFERENCES MODULE(id)
);

CREATE TABLE EEPROM_MAP (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    module_id INTEGER NOT NULL,
    address_start TEXT NOT NULL,
    address_end TEXT NOT NULL,
    region_name TEXT NOT NULL,
    description TEXT,
    data_type TEXT,          -- 'Patch', 'ROM_ID', 'Coding', 'IMMO', 'Odometer', 'Checksum', 'PROTECTED'
    known_values TEXT,
    is_protected INTEGER DEFAULT 0,    -- V2: 1=área protegida, não pode ser sobrescrita
    is_checksum INTEGER DEFAULT 0,     -- V2: 1=esta região contém bytes de checksum
    checksum_covers_start TEXT,        -- V2: range que este checksum protege
    checksum_covers_end TEXT,
    source_document TEXT NOT NULL,
    source_version TEXT,
    source_date TEXT,
    FOREIGN KEY (module_id) REFERENCES MODULE(id)
);

CREATE TABLE PATCH (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    name TEXT NOT NULL,
    description TEXT,
    category TEXT,
    author TEXT,
    license TEXT
);

CREATE TABLE PATCH_VARIANT (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    patch_id INTEGER NOT NULL,
    rom_version_id INTEGER NOT NULL,
    software_version TEXT,
    address_start TEXT NOT NULL,
    address_end TEXT NOT NULL,
    patch_data BLOB NOT NULL,
    patch_data_hex TEXT NOT NULL,
    cluster_level TEXT,
    preconditions TEXT,
    safeguards TEXT,
    source_document TEXT NOT NULL,
    source_version TEXT,
    source_date TEXT,
    FOREIGN KEY (patch_id) REFERENCES PATCH(id),
    FOREIGN KEY (rom_version_id) REFERENCES ROM_VERSION(id)
);

-- V2: Patches adicionados manualmente pelo operador (não fazem parte da KB oficial)
CREATE TABLE OPERATOR_PATCH (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    module_address TEXT NOT NULL,       -- endereço VAG do módulo (17, 46, etc.)
    target_rom_id TEXT NOT NULL,        -- ROM ID alvo
    patch_name TEXT NOT NULL,
    patch_description TEXT,
    address_start TEXT NOT NULL,
    address_end TEXT NOT NULL,
    patch_data BLOB NOT NULL,
    patch_data_hex TEXT NOT NULL,
    notes TEXT,
    verified INTEGER DEFAULT 0,         -- 0=não testado, 1=testado pelo operador
    created_at TEXT NOT NULL,
    updated_at TEXT NOT NULL
);

CREATE TABLE CODING (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    platform_id INTEGER NOT NULL,
    module_address TEXT NOT NULL,
    coding_type TEXT NOT NULL,
    feature_name TEXT NOT NULL,
    byte_address TEXT,
    bit_number TEXT,
    original_value TEXT,
    modified_value TEXT,
    description TEXT,
    source_document TEXT NOT NULL,
    source_version TEXT,
    source_date TEXT,
    FOREIGN KEY (platform_id) REFERENCES PLATFORM(id)
);

CREATE TABLE EEPROM_FEATURE (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    module_id INTEGER NOT NULL,
    feature_name TEXT NOT NULL,
    software_version TEXT,
    eeprom_address TEXT NOT NULL,
    original_value TEXT,
    modified_value TEXT,
    bit_details TEXT,
    notes TEXT,
    source_document TEXT NOT NULL,
    source_version TEXT,
    source_date TEXT,
    FOREIGN KEY (module_id) REFERENCES MODULE(id)
);

CREATE TABLE RETROFIT (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    name TEXT NOT NULL,
    compatible_vehicles TEXT,
    dependencies TEXT,
    required_parts TEXT,
    coding_changes TEXT,
    observations TEXT,
    source_document TEXT NOT NULL,
    source_version TEXT,
    source_date TEXT
);

CREATE TABLE COMPATIBILITY_MATRIX (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    feature TEXT NOT NULL,
    panel_immo2_status TEXT,
    panel_immo3_status TEXT,
    pq35_status TEXT,
    source_document TEXT NOT NULL,
    source_version TEXT,
    source_date TEXT
);

CREATE TABLE KNOWN_CONFLICT (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    title TEXT NOT NULL,
    source_a TEXT,
    source_a_claim TEXT,
    source_b TEXT,
    source_b_claim TEXT,
    resolution_status TEXT,
    resolution_notes TEXT
);

CREATE TABLE DUMP_METADATA (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    dump_hash_sha256 TEXT NOT NULL,
    dump_filename TEXT NOT NULL,
    vin TEXT,
    pin_skc TEXT,
    keys_adapted INTEGER,
    mileage_km INTEGER,
    immo_status TEXT,
    external_software TEXT NOT NULL,
    external_software_version TEXT,
    user_notes TEXT,
    created_at TEXT NOT NULL,
    updated_at TEXT NOT NULL,
    source_description TEXT
);

CREATE TABLE TECHNICIAN_MODULE_ENTRY (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    part_number TEXT NOT NULL,
    manufacturer TEXT NOT NULL,
    platform TEXT NOT NULL,
    software_version TEXT,
    rom_id TEXT,
    sticker_code TEXT,
    eeprom_type TEXT,
    eeprom_size_bytes INTEGER,
    immo_version TEXT NOT NULL,
    pin_skc TEXT,
    compatible_vehicles TEXT,
    known_features TEXT,
    known_limitations TEXT,
    external_software TEXT NOT NULL,
    external_software_version TEXT,
    technician_notes TEXT,
    status TEXT NOT NULL,
    created_at TEXT NOT NULL,
    updated_at TEXT NOT NULL
);

CREATE TABLE KB_VERSION (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    version TEXT NOT NULL,
    schema_version TEXT NOT NULL DEFAULT '2.0',  -- V2: rastrear versão do schema
    release_date TEXT,
    changelog TEXT,
    hash_sha256 TEXT
);

CREATE TABLE AUDIT_LOG (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    timestamp TEXT NOT NULL,
    operation_type TEXT NOT NULL,
    dump_filename TEXT NOT NULL,
    dump_hash_before TEXT,
    dump_hash_after TEXT,
    module_identified TEXT,
    patch_applied TEXT,
    patch_variant_id INTEGER,
    result TEXT NOT NULL,
    block_reason TEXT,
    diff_hex TEXT,
    -- V2: campos adicionais de auditoria
    module_address TEXT,
    checksum_status TEXT,   -- 'VALID', 'INVALID', 'UNKNOWN', 'RECALCULATED'
    kline_operation TEXT,   -- 'DUMP', 'READ', 'WRITE', 'GET_SKC' (para Feature 3)
    user_notes TEXT
);
