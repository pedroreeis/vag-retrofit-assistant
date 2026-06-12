package com.vagretrofit.service;

import com.vagretrofit.kline.KLineConnection;
import com.vagretrofit.kline.Kw1281Commands;
import com.vagretrofit.kline.Kw1281Protocol;

import java.io.IOException;
import java.util.function.Consumer;

/**
 * V2: Serviço de alto nível para operações K-Line VAG.
 * Integra KLineConnection + Kw1281Protocol + Kw1281Commands.
 * Registra todas as operações no AuditService.
 */
public class KLineService {

    public static final int DEFAULT_BAUD_PUBLIC = KLineConnection.DEFAULT_BAUD;
    private KLineConnection connection;
    private Kw1281Protocol protocol;
    private Kw1281Commands commands;
    private final AuditService auditService;
    private Consumer<String> logCallback;

    public KLineService() {
        this.auditService = new AuditService();
    }

    public void setLogCallback(Consumer<String> callback) {
        this.logCallback = callback;
    }

    private void log(String msg) {
        System.out.println("[KLINE-SVC] " + msg);
        if (logCallback != null) logCallback.accept(msg);
    }

    // ─── Conexão ────────────────────────────────────────────────────────────

    public void connect(String portName, int moduleAddress, int baudRate) throws IOException {
        log("Conectando a " + portName + " → módulo 0x" + String.format("%02X", moduleAddress) + " @ " + baudRate + " baud...");
        connection = new KLineConnection(baudRate);
        connection.open(portName, moduleAddress);
        protocol  = new Kw1281Protocol(connection);
        commands  = new Kw1281Commands(protocol);
        commands.setProgressCallback(this::log);
        log("Conexão estabelecida.");
    }

    public void disconnect() {
        if (connection != null && connection.isConnected()) {
            try {
                protocol.sendEndCommunication();
            } catch (Exception ignored) {}
            connection.close();
            log("Desconectado.");
        }
    }

    public boolean isConnected() {
        return connection != null && connection.isConnected();
    }

    // ─── Operações EEPROM ───────────────────────────────────────────────────

    /**
     * Faz dump completo da EEPROM do módulo conectado.
     */
    public byte[] dumpEeprom(int eepromSize, String moduleId) throws IOException {
        requireConnection();
        log("DumpEeprom iniciado para módulo: " + moduleId);
        try {
            byte[] data = commands.dumpEeprom(eepromSize);
            auditService.logOperation("KLINE_DUMP", moduleId, null, null, moduleId, null, "SUCCESS",
                    "KLINE_DUMP: " + eepromSize + " bytes lidos");
            return data;
        } catch (IOException e) {
            auditService.logOperation("KLINE_DUMP", moduleId, null, null, moduleId, null, "ERROR", e.getMessage());
            throw e;
        }
    }

    /**
     * Lê bytes específicos da EEPROM.
     */
    public byte[] readEeprom(int address, int length, String moduleId) throws IOException {
        requireConnection();
        log(String.format("ReadEeprom: 0x%04X, %d bytes", address, length));
        try {
            byte[] data = commands.readEeprom(address, length);
            auditService.logOperation("KLINE_READ", moduleId, null, null, moduleId, null, "SUCCESS",
                    String.format("KLINE_READ: 0x%04X +%d bytes", address, length));
            return data;
        } catch (IOException e) {
            auditService.logOperation("KLINE_READ", moduleId, null, null, moduleId, null, "ERROR", e.getMessage());
            throw e;
        }
    }

    /**
     * Escreve bytes na EEPROM (com backup obrigatório implícito — caller deve ter feito backup).
     */
    public void writeEeprom(int address, byte[] data, String moduleId) throws IOException {
        requireConnection();
        log(String.format("WriteEeprom: 0x%04X, %d bytes", address, data.length));
        try {
            commands.writeEeprom(address, data);
            auditService.logOperation("KLINE_WRITE", moduleId, null, null, moduleId, null, "SUCCESS",
                    String.format("KLINE_WRITE: 0x%04X +%d bytes", address, data.length));
        } catch (IOException e) {
            auditService.logOperation("KLINE_WRITE", moduleId, null, null, moduleId, null, "ERROR", e.getMessage());
            throw e;
        }
    }

    /**
     * Recupera o SKC (PIN) do imobilizador.
     */
    public String getSKC(String moduleId) throws IOException {
        requireConnection();
        log("GetSKC para módulo: " + moduleId);
        try {
            String skc = commands.getSKC();
            auditService.logOperation("KLINE_GET_SKC", moduleId, null, null, moduleId, null,
                    skc != null ? "SUCCESS" : "NOT_SUPPORTED",
                    "KLINE_GET_SKC: " + (skc != null ? "obtido" : "não suportado"));
            return skc;
        } catch (IOException e) {
            auditService.logOperation("KLINE_GET_SKC", moduleId, null, null, moduleId, null, "ERROR", e.getMessage());
            throw e;
        }
    }

    private void requireConnection() throws IOException {
        if (!isConnected()) {
            throw new IOException("Não conectado. Use connect() antes de executar operações.");
        }
    }
}
