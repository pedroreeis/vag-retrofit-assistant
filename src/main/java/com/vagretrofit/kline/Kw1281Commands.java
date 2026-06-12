package com.vagretrofit.kline;

import java.io.IOException;
import java.util.Arrays;
import java.util.function.Consumer;

/**
 * V2: Comandos de alto nível kw1281test portados de C# para Java.
 *
 * Escopo implementado (conforme decisão do usuário):
 * - DumpEeprom   — lê a EEPROM completa
 * - ReadEeprom   — lê bytes específicos
 * - WriteEeprom  — escreve bytes na EEPROM
 * - GetSKC       — recupera o Security Key Code (PIN)
 *
 * Referência: https://github.com/gmenounos/kw1281test
 */
public class Kw1281Commands {

    private final Kw1281Protocol protocol;
    private Consumer<String> progressCallback; // callback para log na UI

    public Kw1281Commands(Kw1281Protocol protocol) {
        this.protocol = protocol;
    }

    public void setProgressCallback(Consumer<String> callback) {
        this.progressCallback = callback;
    }

    private void log(String msg) {
        System.out.println("[KW1281] " + msg);
        if (progressCallback != null) progressCallback.accept(msg);
    }

    // ─── DumpEeprom ─────────────────────────────────────────────────────────

    /**
     * Lê a EEPROM completa do módulo, byte a byte.
     * @param eepromSize tamanho total em bytes (ex: 2048 para 24C02)
     * @return array de bytes com o conteúdo completo da EEPROM
     */
    public byte[] dumpEeprom(int eepromSize) throws IOException {
        log("Iniciando DumpEeprom (" + eepromSize + " bytes)...");
        byte[] dump = new byte[eepromSize];
        int bytesRead = 0;

        // Lê em blocos de até 16 bytes por vez (limite KW1281)
        final int CHUNK_SIZE = 16;
        while (bytesRead < eepromSize) {
            int address = bytesRead;
            int length  = Math.min(CHUNK_SIZE, eepromSize - bytesRead);
            byte[] chunk = readEeprom(address, length);
            System.arraycopy(chunk, 0, dump, bytesRead, chunk.length);
            bytesRead += chunk.length;

            // Progress
            int pct = (bytesRead * 100) / eepromSize;
            log(String.format("  0x%04X / 0x%04X (%d%%)", bytesRead, eepromSize, pct));
        }

        log("DumpEeprom concluído — " + bytesRead + " bytes lidos.");
        return dump;
    }

    // ─── ReadEeprom ─────────────────────────────────────────────────────────

    /**
     * Lê bytes específicos da EEPROM.
     * @param address offset inicial (ex: 0x4F8)
     * @param length  quantidade de bytes a ler
     */
    public byte[] readEeprom(int address, int length) throws IOException {
        log(String.format("ReadEeprom: 0x%04X, %d bytes", address, length));

        // Bloco de request: [addr_high][addr_low][length]
        byte[] requestData = {
            (byte) ((address >> 8) & 0xFF),
            (byte) (address & 0xFF),
            (byte) (length & 0xFF)
        };

        protocol.sendBlock(Kw1281Protocol.BLOCK_READ_EEPROM, requestData);

        // Aguardar resposta da ECU
        Kw1281Protocol.Kw1281Block response = protocol.receiveBlock();

        if (response.title != Kw1281Protocol.BLOCK_READ_EEPROM_R) {
            throw new IOException(String.format(
                "Resposta inesperada para ReadEeprom: 0x%02X (esperado 0x%02X)",
                response.title, Kw1281Protocol.BLOCK_READ_EEPROM_R));
        }

        // Enviar ACK
        protocol.sendAck();

        byte[] result = Arrays.copyOfRange(response.data, 0, Math.min(length, response.data.length));
        log(String.format("ReadEeprom OK: %d bytes a partir de 0x%04X", result.length, address));
        return result;
    }

    // ─── WriteEeprom ────────────────────────────────────────────────────────

    /**
     * Escreve bytes na EEPROM.
     * @param address offset inicial
     * @param data    bytes a escrever
     */
    public void writeEeprom(int address, byte[] data) throws IOException {
        log(String.format("WriteEeprom: 0x%04X, %d bytes", address, data.length));

        // Escreve em blocos de até 16 bytes por vez
        final int CHUNK_SIZE = 16;
        int written = 0;

        while (written < data.length) {
            int chunkAddr  = address + written;
            int chunkLen   = Math.min(CHUNK_SIZE, data.length - written);
            byte[] chunk   = Arrays.copyOfRange(data, written, written + chunkLen);

            // Bloco de write: [addr_high][addr_low][data...]
            byte[] requestData = new byte[2 + chunkLen];
            requestData[0] = (byte) ((chunkAddr >> 8) & 0xFF);
            requestData[1] = (byte) (chunkAddr & 0xFF);
            System.arraycopy(chunk, 0, requestData, 2, chunkLen);

            protocol.sendBlock(Kw1281Protocol.BLOCK_WRITE_EEPROM, requestData);

            // Aguardar resposta/ACK
            Kw1281Protocol.Kw1281Block response = protocol.receiveBlock();

            if (response.title != Kw1281Protocol.BLOCK_WRITE_EEPROM_R && !response.isAck()) {
                throw new IOException(String.format(
                    "Resposta inesperada para WriteEeprom: 0x%02X", response.title));
            }

            protocol.sendAck();
            written += chunkLen;
            log(String.format("  Escrito: 0x%04X (%d/%d bytes)", chunkAddr, written, data.length));
        }

        log("WriteEeprom concluído — " + written + " bytes escritos.");
    }

    // ─── GetSKC ─────────────────────────────────────────────────────────────

    /**
     * Recupera o Security Key Code (SKC / PIN) do imobilizador.
     * O SKC é um valor de 4 dígitos usado para adaptar chaves e sincronizar o IMMO.
     *
     * NOTA: Função disponível apenas em módulos que suportam Login Block (0x2B).
     * @return SKC como string de 4 dígitos (ex: "1234"), ou null se não suportado
     */
    public String getSKC() throws IOException {
        log("GetSKC: Solicitando Security Key Code...");

        // Login block: [0x00][0x00][0x00][0x00] — solicita SKC
        byte[] loginData = {0x00, 0x00, 0x00, 0x00};
        protocol.sendBlock(Kw1281Protocol.BLOCK_READ_LOGIN, loginData);

        Kw1281Protocol.Kw1281Block response = protocol.receiveBlock();

        if (response.isAck()) {
            // ECU respondeu com ACK — tentar ler via adaptation channel
            protocol.sendAck();
            // Alguns módulos retornam SKC em adaptation channel 0x18
            return readSkcFromAdaptation();
        }

        if (response.data != null && response.data.length >= 2) {
            // SKC geralmente nos 2 primeiros bytes da resposta
            int skc = ((response.data[0] & 0xFF) << 8) | (response.data[1] & 0xFF);
            String skcStr = String.format("%05d", skc);
            protocol.sendAck();
            log("SKC obtido: " + skcStr);
            return skcStr;
        }

        protocol.sendAck();
        log("GetSKC: Resposta não reconhecida para este módulo.");
        return null;
    }

    private String readSkcFromAdaptation() throws IOException {
        // Adaptation channel 0x18 é onde alguns painéis VDO armazenam o SKC
        // Enviamos bloco de leitura de adaptation (0x28) com channel=0x18
        byte[] adaptData = {0x28, 0x18};
        protocol.sendBlock(0x28, new byte[]{0x18});

        Kw1281Protocol.Kw1281Block response = protocol.receiveBlock();

        if (response.data != null && response.data.length >= 2) {
            int skc = ((response.data[0] & 0xFF) << 8) | (response.data[1] & 0xFF);
            String skcStr = String.format("%05d", skc);
            protocol.sendAck();
            log("SKC via adaptation: " + skcStr);
            return skcStr;
        }

        protocol.sendAck();
        return null;
    }
}
