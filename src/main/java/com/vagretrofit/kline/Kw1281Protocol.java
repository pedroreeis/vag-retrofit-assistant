package com.vagretrofit.kline;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * V2: Implementação do protocolo KW1281 sobre a camada serial K-Line.
 *
 * Estrutura de bloco KW1281:
 * [LENGTH] [COUNTER] [TITLE] [DATA...] [END=0x03]
 *
 * Títulos relevantes:
 * - 0x09: ACK
 * - 0x06: End of communication
 * - 0x19: ReadEeprom request
 * - 0xFD: ReadEeprom response
 * - 0x0F: WriteEeprom request
 * - 0x0E: WriteEeprom response
 * - 0x07: ReadFaultCodes
 * - 0x2B: Login
 * - 0x04: Coding
 */
public class Kw1281Protocol {

    // Block title bytes
    public static final int BLOCK_ACK            = 0x09;
    public static final int BLOCK_END            = 0x06;
    public static final int BLOCK_READ_EEPROM    = 0x19;
    public static final int BLOCK_READ_EEPROM_R  = 0xFD;
    public static final int BLOCK_WRITE_EEPROM   = 0x0F;
    public static final int BLOCK_WRITE_EEPROM_R = 0x0E;
    public static final int BLOCK_EOF            = 0x03; // end-of-block marker
    public static final int BLOCK_READ_LOGIN     = 0x2B;

    private final KLineConnection connection;
    private int blockCounter = 1;

    public Kw1281Protocol(KLineConnection connection) {
        this.connection = connection;
    }

    // ─── Envio de Blocos ────────────────────────────────────────────────────

    /**
     * Envia um bloco KW1281 completo.
     * Formato: [LENGTH][COUNTER][TITLE][DATA...][0x03]
     * Entre cada byte, aguarda o eco da ECU e envia ACK invertido.
     */
    public void sendBlock(int title, byte[] data) throws IOException {
        int length = (data != null ? data.length : 0) + 3; // title + counter + end

        sendByteAndWaitEcho(length & 0xFF);
        sendByteAndWaitEcho(blockCounter & 0xFF);
        sendByteAndWaitEcho(title & 0xFF);

        if (data != null) {
            for (byte b : data) {
                sendByteAndWaitEcho(b & 0xFF);
            }
        }

        // End of block (0x03) — NÃO aguarda eco do último byte
        connection.sendByte(BLOCK_EOF);
        blockCounter = (blockCounter + 1) & 0xFF;
    }

    /**
     * Envia ACK simples.
     */
    public void sendAck() throws IOException {
        sendBlock(BLOCK_ACK, null);
    }

    /**
     * Envia bloco de fim de comunicação.
     */
    public void sendEndCommunication() throws IOException {
        sendBlock(BLOCK_END, null);
        blockCounter = 1;
    }

    // ─── Recebimento de Blocos ──────────────────────────────────────────────

    /**
     * Lê um bloco KW1281 da ECU.
     * @return Kw1281Block com título e dados, ou null se ACK
     */
    public Kw1281Block receiveBlock() throws IOException {
        int length  = connection.readByteWithTimeout(1000);
        sendComplement(length);

        int counter = connection.readByteWithTimeout(1000);
        sendComplement(counter);

        int title = connection.readByteWithTimeout(1000);
        sendComplement(title);

        int dataLength = length - 3;
        byte[] data = new byte[Math.max(0, dataLength)];
        for (int i = 0; i < dataLength; i++) {
            int b = connection.readByteWithTimeout(1000);
            sendComplement(b);
            data[i] = (byte) b;
        }

        // End byte (0x03)
        connection.readByteWithTimeout(500);

        return new Kw1281Block(title, data, counter);
    }

    /**
     * Envia o complemento (bitwise NOT) de um byte — protocolo KW1281.
     */
    private void sendComplement(int b) throws IOException {
        connection.sendByte((~b) & 0xFF);
    }

    /**
     * Envia um byte e aguarda o eco da ECU.
     */
    private void sendByteAndWaitEcho(int b) throws IOException {
        connection.sendByte(b);
        // ECU faz eco do byte — ler e descartar
        connection.readByteWithTimeout(500);
    }

    // ─── Inner class: Block ─────────────────────────────────────────────────

    public static class Kw1281Block {
        public final int title;
        public final byte[] data;
        public final int counter;

        public Kw1281Block(int title, byte[] data, int counter) {
            this.title   = title;
            this.data    = data;
            this.counter = counter;
        }

        public boolean isAck() { return title == BLOCK_ACK; }
        public boolean isEnd() { return title == BLOCK_END; }

        @Override
        public String toString() {
            return String.format("Block[title=0x%02X, counter=%d, dataLen=%d]",
                title, counter, data != null ? data.length : 0);
        }
    }
}
