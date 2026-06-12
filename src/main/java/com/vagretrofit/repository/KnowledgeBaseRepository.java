package com.vagretrofit.repository;

import com.vagretrofit.domain.Module;
import com.vagretrofit.domain.RomVersion;
import com.vagretrofit.domain.Patch;
import com.vagretrofit.domain.PatchVariant;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * V2: Repositório da Knowledge Base com suporte multi-módulo.
 * Adiciona findModulesByAddress, findAllModuleAddresses, e queries genéricas por endereço.
 */
public class KnowledgeBaseRepository {

    // ─── PATCH VARIANTS ────────────────────────────────────────────────────────

    public List<PatchVariant> findPatchesForRomVersion(RomVersion romVersion) {
        List<PatchVariant> variants = new ArrayList<>();
        String sql = "SELECT pv.id AS pv_id, pv.software_version, pv.address_start, pv.address_end, " +
                     "pv.patch_data, pv.cluster_level, pv.preconditions, pv.safeguards, " +
                     "p.id AS p_id, p.name, p.description, p.category, p.author " +
                     "FROM PATCH_VARIANT pv " +
                     "JOIN PATCH p ON pv.patch_id = p.id " +
                     "WHERE pv.rom_version_id = ?";

        try (Connection conn = DatabaseManager.getConnectionKb();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, romVersion.getId());
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    variants.add(mapPatchVariant(rs, romVersion));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return variants;
    }

    // ─── ROM VERSIONS ───────────────────────────────────────────────────────────

    public Optional<RomVersion> findRomVersionByRomId(String romIdHex) {
        String sql = "SELECT r.id AS rom_id, r.rom_id AS rom_hex, r.sticker_code, r.rom_id_offset, " +
                     "m.id AS module_id, m.part_number, m.software_version, m.eeprom_type, " +
                     "m.eeprom_size_bytes, m.immo, m.module_address, " +
                     "m.checksum_algorithm, m.checksum_count, m.checksum_requires_flash, " +
                     "p.code AS platform_code, mt.manufacturer " +
                     "FROM ROM_VERSION r " +
                     "JOIN MODULE m ON r.module_id = m.id " +
                     "JOIN PLATFORM p ON m.platform_id = p.id " +
                     "JOIN MODULE_TYPE mt ON m.module_type_id = mt.id " +
                     "WHERE r.rom_id = ?";

        try (Connection conn = DatabaseManager.getConnectionKb();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, romIdHex);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRomVersion(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return Optional.empty();
    }

    // ─── V2: MULTI-MODULE ───────────────────────────────────────────────────────

    /**
     * Retorna todos os módulos cadastrados para um dado endereço VAG (ex: "17", "46").
     */
    public List<Module> findModulesByAddress(String moduleAddress) {
        List<Module> modules = new ArrayList<>();
        String sql = "SELECT m.id AS module_id, m.part_number, m.software_version, m.sw_version_code, " +
                     "m.eeprom_type, m.eeprom_size_bytes, m.immo, m.module_address, " +
                     "m.checksum_algorithm, m.checksum_count, m.checksum_requires_flash, " +
                     "m.features, m.limitations, " +
                     "p.code AS platform_code, mt.manufacturer " +
                     "FROM MODULE m " +
                     "JOIN PLATFORM p ON m.platform_id = p.id " +
                     "JOIN MODULE_TYPE mt ON m.module_type_id = mt.id " +
                     "WHERE m.module_address = ?";

        try (Connection conn = DatabaseManager.getConnectionKb();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, moduleAddress);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) modules.add(mapModule(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return modules;
    }

    /**
     * Retorna todos os endereços de módulo distintos presentes na KB.
     * Mapa: código → descrição (ex: "17" → "Painel de Instrumentos").
     */
    public Map<String, Long> findAllModuleAddressesWithCount() {
        Map<String, Long> result = new LinkedHashMap<>();
        String sql = "SELECT module_address, COUNT(*) AS cnt FROM MODULE GROUP BY module_address ORDER BY module_address";
        try (Connection conn = DatabaseManager.getConnectionKb();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) {
                result.put(rs.getString("module_address"), rs.getLong("cnt"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return result;
    }

    /**
     * Retorna todos os módulos cadastrados na KB.
     */
    public List<Module> findAllModules() {
        List<Module> modules = new ArrayList<>();
        String sql = "SELECT m.id AS module_id, m.part_number, m.software_version, m.sw_version_code, " +
                     "m.eeprom_type, m.eeprom_size_bytes, m.immo, m.module_address, " +
                     "m.checksum_algorithm, m.checksum_count, m.checksum_requires_flash, " +
                     "m.features, m.limitations, " +
                     "p.code AS platform_code, mt.manufacturer " +
                     "FROM MODULE m " +
                     "JOIN PLATFORM p ON m.platform_id = p.id " +
                     "JOIN MODULE_TYPE mt ON m.module_type_id = mt.id " +
                     "ORDER BY m.module_address, m.part_number";

        try (Connection conn = DatabaseManager.getConnectionKb();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) modules.add(mapModule(rs));
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return modules;
    }

    // ─── MAPPERS ────────────────────────────────────────────────────────────────

    private Module mapModule(ResultSet rs) throws SQLException {
        Module module = new Module();
        module.setId(rs.getInt("module_id"));
        module.setPartNumber(rs.getString("part_number"));
        // Tenta software_version primeiro, depois sw_version_code
        String sv = rs.getString("software_version");
        if (sv == null || sv.isEmpty()) sv = rs.getString("sw_version_code");
        module.setSoftwareVersion(sv);
        module.setEepromType(rs.getString("eeprom_type"));
        module.setEepromSizeBytes(rs.getInt("eeprom_size_bytes"));
        module.setImmo(rs.getString("immo"));
        module.setPlatformCode(rs.getString("platform_code"));
        module.setManufacturer(rs.getString("manufacturer"));
        // V2
        module.setModuleAddress(rs.getString("module_address"));
        module.setChecksumAlgorithm(rs.getString("checksum_algorithm"));
        module.setChecksumCount(rs.getInt("checksum_count"));
        module.setChecksumRequiresFlash(rs.getInt("checksum_requires_flash") == 1);
        return module;
    }

    private RomVersion mapRomVersion(ResultSet rs) throws SQLException {
        Module module = mapModule(rs);

        RomVersion romVersion = new RomVersion();
        romVersion.setId(rs.getInt("rom_id"));
        romVersion.setRomId(rs.getString("rom_hex"));
        romVersion.setStickerCode(rs.getString("sticker_code"));
        romVersion.setRomIdOffset(rs.getString("rom_id_offset"));
        romVersion.setModule(module);
        return romVersion;
    }

    private PatchVariant mapPatchVariant(ResultSet rs, RomVersion romVersion) throws SQLException {
        Patch patch = new Patch();
        patch.setId(rs.getInt("p_id"));
        patch.setName(rs.getString("name"));
        patch.setDescription(rs.getString("description"));
        patch.setCategory(rs.getString("category"));
        patch.setAuthor(rs.getString("author"));

        PatchVariant variant = new PatchVariant();
        variant.setId(rs.getInt("pv_id"));
        variant.setPatch(patch);
        variant.setTargetRomVersion(romVersion);
        variant.setSoftwareVersion(rs.getString("software_version"));

        String addrStart = rs.getString("address_start");
        String addrEnd   = rs.getString("address_end");
        variant.setAddressStart(addrStart.startsWith("0x") ? Integer.decode(addrStart) : Integer.parseInt(addrStart));
        variant.setAddressEnd(addrEnd.startsWith("0x") ? Integer.decode(addrEnd) : Integer.parseInt(addrEnd));

        variant.setPatchData(rs.getBytes("patch_data"));
        variant.setClusterLevel(rs.getString("cluster_level"));
        variant.setPreconditions(rs.getString("preconditions"));
        variant.setSafeguards(rs.getString("safeguards"));

        return variant;
    }
}
