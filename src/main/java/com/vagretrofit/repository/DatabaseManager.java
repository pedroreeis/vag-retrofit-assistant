package com.vagretrofit.repository;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Gerencia as conexões SQLite da Knowledge Base e do Audit Log.
 * FAIL SAFE FIRST: Qualquer falha na inicialização bloqueia a aplicação.
 *
 * BUG-01 FIX: Caminhos absolutos baseados em user.home (AppData) para suportar jpackage.
 * BUG-02 FIX: Métodos de conexão abrem e fecham dentro de try-with-resources.
 * V2: Migration automática de schema V1 → V2.
 */
public class DatabaseManager {

    private static final String APP_DATA_DIR;
    // V2: novo arquivo de KB para não corromper bases V1 existentes
    private static final String DB_FILE_V2   = "knowledge-base-v2.0.0.db";
    private static final String DB_FILE_V1   = "knowledge-base-v1.0.0.db";
    private static final String AUDIT_FILE   = "audit-log.db";
    private static String JDBC_URL_KB;
    private static String JDBC_URL_AUDIT;

    static {
        String userHome = System.getProperty("user.home");
        APP_DATA_DIR = userHome + File.separator + ".vagretrofit";
        JDBC_URL_KB    = "jdbc:sqlite:" + APP_DATA_DIR + File.separator + "db"    + File.separator + DB_FILE_V2;
        JDBC_URL_AUDIT = "jdbc:sqlite:" + APP_DATA_DIR + File.separator + "audit" + File.separator + AUDIT_FILE;
    }

    public static void initialize() {
        try {
            ensureDirectories();

            File v2DbFile    = new File(APP_DATA_DIR + File.separator + "db" + File.separator + DB_FILE_V2);
            File auditDbFile = new File(APP_DATA_DIR + File.separator + "audit" + File.separator + AUDIT_FILE);

            if (!v2DbFile.exists()) {
                System.out.println("[DB] Inicializando Knowledge Base V2 em: " + APP_DATA_DIR);
                try (Connection conn = getConnectionKb()) {
                    executeSqlFile(conn, "/db/schema.sql");
                    executeSqlFile(conn, "/db/seed-data.sql");
                }
            } else {
                // V2: Verificar se migration é necessária
                migrateIfNeeded();
            }

            if (!auditDbFile.exists()) {
                System.out.println("[DB] Inicializando banco de Auditoria V2...");
                try (Connection conn = getConnectionAudit();
                     Statement stmt = conn.createStatement()) {
                    stmt.execute("PRAGMA journal_mode=WAL");
                    stmt.execute(
                        "CREATE TABLE IF NOT EXISTS AUDIT_LOG (" +
                        "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                        "timestamp TEXT NOT NULL," +
                        "operation_type TEXT NOT NULL," +
                        "dump_filename TEXT NOT NULL," +
                        "dump_hash_before TEXT," +
                        "dump_hash_after TEXT," +
                        "module_identified TEXT," +
                        "patch_applied TEXT," +
                        "patch_variant_id INTEGER," +
                        "result TEXT NOT NULL," +
                        "block_reason TEXT," +
                        "diff_hex TEXT," +
                        "module_address TEXT," +
                        "checksum_status TEXT," +
                        "kline_operation TEXT," +
                        "user_notes TEXT" +
                        ")"
                    );
                }
            } else {
                // V2: Adicionar colunas novas ao audit se não existirem
                migrateAuditIfNeeded();
            }

            System.out.println("[DB] Bancos de dados prontos (V2).");

        } catch (Exception e) {
            System.err.println("[FAIL SAFE] Falha CRÍTICA ao inicializar bancos de dados: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }

    /**
     * V2: Migration automática do schema — adiciona colunas novas sem perder dados existentes.
     */
    private static void migrateIfNeeded() {
        try (Connection conn = getConnectionKb();
             Statement stmt = conn.createStatement()) {

            System.out.println("[DB] Verificando necessidade de migration V1→V2...");

            // Checar se a coluna module_address existe na tabela MODULE
            boolean hasModuleAddress = columnExists(conn, "MODULE", "module_address");
            if (!hasModuleAddress) {
                System.out.println("[DB] Migration: Adicionando colunas V2 à tabela MODULE...");
                stmt.execute("ALTER TABLE MODULE ADD COLUMN module_address TEXT DEFAULT '17'");
                stmt.execute("ALTER TABLE MODULE ADD COLUMN checksum_algorithm TEXT");
                stmt.execute("ALTER TABLE MODULE ADD COLUMN checksum_count INTEGER DEFAULT 1");
                stmt.execute("ALTER TABLE MODULE ADD COLUMN checksum_requires_flash INTEGER DEFAULT 0");
            }

            // Checar se OPERATOR_PATCH existe
            boolean hasOperatorPatch = tableExists(conn, "OPERATOR_PATCH");
            if (!hasOperatorPatch) {
                System.out.println("[DB] Migration: Criando tabela OPERATOR_PATCH...");
                stmt.execute(
                    "CREATE TABLE OPERATOR_PATCH (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "module_address TEXT NOT NULL," +
                    "target_rom_id TEXT NOT NULL," +
                    "patch_name TEXT NOT NULL," +
                    "patch_description TEXT," +
                    "address_start TEXT NOT NULL," +
                    "address_end TEXT NOT NULL," +
                    "patch_data BLOB NOT NULL," +
                    "patch_data_hex TEXT NOT NULL," +
                    "notes TEXT," +
                    "verified INTEGER DEFAULT 0," +
                    "created_at TEXT NOT NULL," +
                    "updated_at TEXT NOT NULL" +
                    ")"
                );
            }

            // Checar se EEPROM_MAP tem colunas V2
            boolean hasIsProtected = columnExists(conn, "EEPROM_MAP", "is_protected");
            if (!hasIsProtected) {
                System.out.println("[DB] Migration: Adicionando colunas V2 à tabela EEPROM_MAP...");
                stmt.execute("ALTER TABLE EEPROM_MAP ADD COLUMN is_protected INTEGER DEFAULT 0");
                stmt.execute("ALTER TABLE EEPROM_MAP ADD COLUMN is_checksum INTEGER DEFAULT 0");
                stmt.execute("ALTER TABLE EEPROM_MAP ADD COLUMN checksum_covers_start TEXT");
                stmt.execute("ALTER TABLE EEPROM_MAP ADD COLUMN checksum_covers_end TEXT");
            }

            // Checar KB_VERSION schema_version
            boolean hasSchemaVersion = columnExists(conn, "KB_VERSION", "schema_version");
            if (!hasSchemaVersion) {
                stmt.execute("ALTER TABLE KB_VERSION ADD COLUMN schema_version TEXT DEFAULT '1.0'");
            }

            System.out.println("[DB] Migration V2 concluída.");

        } catch (Exception e) {
            System.err.println("[DB WARNING] Erro durante migration: " + e.getMessage());
        }
    }

    /**
     * V2: Adiciona colunas V2 ao AUDIT_LOG se não existirem.
     */
    private static void migrateAuditIfNeeded() {
        try (Connection conn = getConnectionAudit();
             Statement stmt = conn.createStatement()) {
            if (!columnExists(conn, "AUDIT_LOG", "module_address")) {
                stmt.execute("ALTER TABLE AUDIT_LOG ADD COLUMN module_address TEXT");
                stmt.execute("ALTER TABLE AUDIT_LOG ADD COLUMN checksum_status TEXT");
                stmt.execute("ALTER TABLE AUDIT_LOG ADD COLUMN kline_operation TEXT");
            }
        } catch (Exception e) {
            System.err.println("[DB WARNING] Erro ao migrar audit log: " + e.getMessage());
        }
    }

    private static boolean tableExists(Connection conn, String tableName) throws SQLException {
        try (ResultSet rs = conn.getMetaData().getTables(null, null, tableName, null)) {
            return rs.next();
        }
    }

    private static boolean columnExists(Connection conn, String tableName, String columnName) throws SQLException {
        try (ResultSet rs = conn.getMetaData().getColumns(null, null, tableName, columnName)) {
            return rs.next();
        }
    }

    public static Connection getConnectionKb() throws SQLException {
        return DriverManager.getConnection(JDBC_URL_KB);
    }

    public static Connection getConnectionAudit() throws SQLException {
        return DriverManager.getConnection(JDBC_URL_AUDIT);
    }

    private static void ensureDirectories() throws IOException {
        Files.createDirectories(Paths.get(APP_DATA_DIR, "db"));
        Files.createDirectories(Paths.get(APP_DATA_DIR, "audit"));
        Files.createDirectories(Paths.get(APP_DATA_DIR, "backups"));
    }

    /**
     * BUG-03 FIX: Split por ";\n" para não quebrar strings SQL com ";" interno.
     */
    private static void executeSqlFile(Connection connection, String resourcePath) throws Exception {
        try (InputStream is = DatabaseManager.class.getResourceAsStream(resourcePath)) {
            if (is == null) {
                throw new RuntimeException("[FAIL SAFE] Recurso SQL não encontrado: " + resourcePath);
            }
            String sql = new String(is.readAllBytes(), StandardCharsets.UTF_8);
            sql = sql.replaceAll("(?m)^\\s*--.*", "");
            String[] statements = sql.split(";\\s*\\n");

            try (Statement stmt = connection.createStatement()) {
                for (String statement : statements) {
                    String trimmed = statement.trim();
                    if (trimmed.isEmpty()) continue;
                    stmt.execute(trimmed);
                }
            }
        }
    }

    public static String getAppDataDir() {
        return APP_DATA_DIR;
    }
}
