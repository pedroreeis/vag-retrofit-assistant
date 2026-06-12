package com.vagretrofit.repository;

import com.vagretrofit.domain.AuditEntry;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.Instant;

public class AuditRepository {

    public void save(AuditEntry entry) {
        String sql = "INSERT INTO AUDIT_LOG (timestamp, operation_type, dump_filename, dump_hash_before, " +
                     "dump_hash_after, module_identified, patch_applied, patch_variant_id, result, " +
                     "block_reason, diff_hex, user_notes) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseManager.getConnectionAudit();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, entry.getTimestamp() != null ? entry.getTimestamp() : Instant.now().toString());
            pstmt.setString(2, entry.getOperationType());
            pstmt.setString(3, entry.getDumpFilename());
            pstmt.setString(4, entry.getDumpHashBefore());
            pstmt.setString(5, entry.getDumpHashAfter());
            pstmt.setString(6, entry.getModuleIdentified());
            pstmt.setString(7, entry.getPatchApplied());
            if (entry.getPatchVariantId() != null) {
                pstmt.setInt(8, entry.getPatchVariantId());
            } else {
                pstmt.setNull(8, java.sql.Types.INTEGER);
            }
            pstmt.setString(9, entry.getResult());
            pstmt.setString(10, entry.getBlockReason());
            pstmt.setString(11, entry.getDiffHex());
            pstmt.setString(12, entry.getUserNotes());

            pstmt.executeUpdate();

        } catch (SQLException e) {
            System.err.println("CRITICAL: Falha ao registrar log de auditoria. Operação bloqueada preventivamente.");
            e.printStackTrace();
            throw new RuntimeException("Falha de auditoria", e);
        }
    }
}
