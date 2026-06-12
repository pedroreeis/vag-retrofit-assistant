package com.vagretrofit.repository;

import com.vagretrofit.domain.OperatorPatchEntry;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * V2: Repositório CRUD para patches adicionados manualmente pelo operador.
 */
public class OperatorPatchRepository {

    public List<OperatorPatchEntry> findAll() {
        List<OperatorPatchEntry> entries = new ArrayList<>();
        String sql = "SELECT * FROM OPERATOR_PATCH ORDER BY created_at DESC";
        try (Connection conn = DatabaseManager.getConnectionKb();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) entries.add(map(rs));
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return entries;
    }

    public List<OperatorPatchEntry> findByModuleAddress(String moduleAddress) {
        List<OperatorPatchEntry> entries = new ArrayList<>();
        String sql = "SELECT * FROM OPERATOR_PATCH WHERE module_address = ? ORDER BY created_at DESC";
        try (Connection conn = DatabaseManager.getConnectionKb();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, moduleAddress);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) entries.add(map(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return entries;
    }

    public List<OperatorPatchEntry> findByModuleAddressAndRomId(String moduleAddress, String romId) {
        List<OperatorPatchEntry> entries = new ArrayList<>();
        String sql = "SELECT * FROM OPERATOR_PATCH WHERE module_address = ? AND target_rom_id = ?";
        try (Connection conn = DatabaseManager.getConnectionKb();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, moduleAddress);
            pstmt.setString(2, romId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) entries.add(map(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return entries;
    }

    public Optional<OperatorPatchEntry> findById(int id) {
        String sql = "SELECT * FROM OPERATOR_PATCH WHERE id = ?";
        try (Connection conn = DatabaseManager.getConnectionKb();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) return Optional.of(map(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return Optional.empty();
    }

    public void save(OperatorPatchEntry entry) {
        String now = Instant.now().toString();
        String sql = "INSERT INTO OPERATOR_PATCH " +
                     "(module_address, target_rom_id, patch_name, patch_description, " +
                     "address_start, address_end, patch_data, patch_data_hex, notes, verified, created_at, updated_at) " +
                     "VALUES (?,?,?,?,?,?,?,?,?,?,?,?)";
        try (Connection conn = DatabaseManager.getConnectionKb();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, entry.getModuleAddress());
            pstmt.setString(2, entry.getTargetRomId());
            pstmt.setString(3, entry.getPatchName());
            pstmt.setString(4, entry.getPatchDescription());
            pstmt.setString(5, String.format("0x%04X", entry.getAddressStart()));
            pstmt.setString(6, String.format("0x%04X", entry.getAddressEnd()));
            pstmt.setBytes(7, entry.getPatchData());
            pstmt.setString(8, entry.getPatchDataHex());
            pstmt.setString(9, entry.getNotes());
            pstmt.setInt(10, entry.isVerified() ? 1 : 0);
            pstmt.setString(11, now);
            pstmt.setString(12, now);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void update(OperatorPatchEntry entry) {
        String now = Instant.now().toString();
        String sql = "UPDATE OPERATOR_PATCH SET patch_name=?, patch_description=?, notes=?, verified=?, updated_at=? WHERE id=?";
        try (Connection conn = DatabaseManager.getConnectionKb();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, entry.getPatchName());
            pstmt.setString(2, entry.getPatchDescription());
            pstmt.setString(3, entry.getNotes());
            pstmt.setInt(4, entry.isVerified() ? 1 : 0);
            pstmt.setString(5, now);
            pstmt.setInt(6, entry.getId());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void delete(int id) {
        String sql = "DELETE FROM OPERATOR_PATCH WHERE id = ?";
        try (Connection conn = DatabaseManager.getConnectionKb();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private OperatorPatchEntry map(ResultSet rs) throws SQLException {
        OperatorPatchEntry e = new OperatorPatchEntry();
        e.setId(rs.getInt("id"));
        e.setModuleAddress(rs.getString("module_address"));
        e.setTargetRomId(rs.getString("target_rom_id"));
        e.setPatchName(rs.getString("patch_name"));
        e.setPatchDescription(rs.getString("patch_description"));
        String addrStart = rs.getString("address_start");
        String addrEnd   = rs.getString("address_end");
        e.setAddressStart(addrStart.startsWith("0x") ? Integer.decode(addrStart) : Integer.parseInt(addrStart));
        e.setAddressEnd(addrEnd.startsWith("0x") ? Integer.decode(addrEnd) : Integer.parseInt(addrEnd));
        e.setPatchData(rs.getBytes("patch_data"));
        e.setPatchDataHex(rs.getString("patch_data_hex"));
        e.setNotes(rs.getString("notes"));
        e.setVerified(rs.getInt("verified") == 1);
        e.setCreatedAt(rs.getString("created_at"));
        e.setUpdatedAt(rs.getString("updated_at"));
        return e;
    }
}
