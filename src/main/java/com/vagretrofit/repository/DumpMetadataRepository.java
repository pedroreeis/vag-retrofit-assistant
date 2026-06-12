package com.vagretrofit.repository;

import com.vagretrofit.domain.DumpMetadata;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.Instant;

public class DumpMetadataRepository {

    public void save(DumpMetadata metadata) {
        String sql = "INSERT INTO DUMP_METADATA (dump_hash_sha256, dump_filename, vin, pin_skc, " +
                     "keys_adapted, mileage_km, immo_status, external_software, external_software_version, " +
                     "user_notes, created_at, updated_at, source_description) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseManager.getConnectionKb();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, metadata.getDumpHashSha256());
            pstmt.setString(2, metadata.getDumpFilename());
            pstmt.setString(3, metadata.getVin());
            pstmt.setString(4, metadata.getPinSkc());
            
            if (metadata.getKeysAdapted() != null) pstmt.setInt(5, metadata.getKeysAdapted());
            else pstmt.setNull(5, java.sql.Types.INTEGER);
            
            if (metadata.getMileageKm() != null) pstmt.setInt(6, metadata.getMileageKm());
            else pstmt.setNull(6, java.sql.Types.INTEGER);
            
            pstmt.setString(7, metadata.getImmoStatus());
            pstmt.setString(8, metadata.getExternalSoftware());
            pstmt.setString(9, metadata.getExternalSoftwareVersion());
            pstmt.setString(10, metadata.getUserNotes());
            
            String now = Instant.now().toString();
            pstmt.setString(11, metadata.getCreatedAt() != null ? metadata.getCreatedAt() : now);
            pstmt.setString(12, now);
            pstmt.setString(13, "Cadastro manual via software externo");

            pstmt.executeUpdate();

        } catch (SQLException e) {
            System.err.println("Erro ao salvar metadados do dump.");
            e.printStackTrace();
        }
    }
}
