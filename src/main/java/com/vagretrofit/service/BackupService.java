package com.vagretrofit.service;

import com.vagretrofit.domain.Dump;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Serviço de backup automático de dumps EEPROM.
 * BUG-01 FIX: Usa o diretório AppData definido pelo DatabaseManager.
 */
public class BackupService {

    public String createBackup(Dump dump) throws IOException {
        // BUG-01 FIX: Usar diretório centralizado em AppData
        String backupDir = Paths.get(
            com.vagretrofit.repository.DatabaseManager.getAppDataDir(), "backups"
        ).toString();

        // Garante que o diretório existe
        Files.createDirectories(Paths.get(backupDir));

        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        String safeHash = dump.getHashSha256() != null
                ? dump.getHashSha256().substring(0, 8) : "NOHASH";
        // Sanitizar nome do arquivo para remover caracteres inválidos no Windows
        String safeFilename = dump.getFilename().replaceAll("[\\\\/:*?\"<>|]", "_");
        String backupFilename = String.format("%s_%s_%s.bak", timestamp, safeHash, safeFilename);

        Path backupPath = Paths.get(backupDir, backupFilename);
        Files.write(backupPath, dump.getData());

        return backupPath.toAbsolutePath().toString();
    }
}
