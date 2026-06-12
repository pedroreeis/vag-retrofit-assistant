package com.vagretrofit.kline;

import com.fazecast.jSerialComm.SerialPort;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * V2: Gerencia a conexão serial via jSerialComm para comunicação K-Line VAG.
 *
 * Handshake KW1281:
 * - 5 Baud Init: endereço do módulo enviado a 5 baud via RTS/DTR bit-banging
 * - Sync byte: ECU responde com 0x55 a ~9600/10400 baud
 * - Keywords: ECU envia 2 bytes de identificação (ex: 0x01 0x8A para KW1281)
 * - ACK invertido
 */
public class KLineConnection {

    public static final int DEFAULT_BAUD = 10400;
    public static final int KW1281_BAUD  = 9600;

    private SerialPort port;
    private InputStream  in;
    private OutputStream out;
    private boolean connected = false;
    private int baudRate;

    public KLineConnection() {
        this.baudRate = DEFAULT_BAUD;
    }

    public KLineConnection(int baudRate) {
        this.baudRate = baudRate;
    }

    /**
     * Lista todas as portas seriais disponíveis no sistema.
     */
    public static String[] listAvailablePorts() {
        SerialPort[] ports = SerialPort.getCommPorts();
        String[] names = new String[ports.length];
        for (int i = 0; i < ports.length; i++) {
            names[i] = ports[i].getSystemPortName() + " — " + ports[i].getDescriptivePortName();
        }
        return names;
    }

    public static SerialPort[] getCommPorts() {
        return SerialPort.getCommPorts();
    }

    /**
     * Abre a conexão serial e realiza o handshake KW1281 com o módulo no endereço dado.
     *
     * @param portName      nome da porta COM (ex: "COM3")
     * @param moduleAddress endereço VAG do módulo (ex: 0x11 para painel = 17 decimal)
     */
    public void open(String portName, int moduleAddress) throws IOException {
        port = SerialPort.getCommPort(portName);
        port.setBaudRate(baudRate);
        port.setNumDataBits(8);
        port.setNumStopBits(SerialPort.ONE_STOP_BIT);
        port.setParity(SerialPort.ODD_PARITY);
        port.setComPortTimeouts(SerialPort.TIMEOUT_READ_BLOCKING, 2000, 0);

        if (!port.openPort()) {
            throw new IOException("Não foi possível abrir a porta serial: " + portName);
        }

        in  = port.getInputStream();
        out = port.getOutputStream();

        try {
            fiveBaudInit(moduleAddress);
            connected = true;
            System.out.println("[K-LINE] Conectado ao módulo 0x" + String.format("%02X", moduleAddress));
        } catch (Exception e) {
            close();
            throw new IOException("Falha no handshake KW1281: " + e.getMessage(), e);
        }
    }

    /**
     * 5-Baud Init: Envia o endereço do módulo bit a bit a 5 baud via bit-banging.
     * O cabo KKL USB simula K-Line via RTS/TXD.
     */
    private void fiveBaudInit(int address) throws Exception {
        // 200ms por bit = 5 baud
        final int BIT_DELAY_MS = 200;

        // Aguarda linha ociosa
        Thread.sleep(300);

        // Enviar endereço a 5 baud via TXD (bit-banging)
        // Start bit (0), 8 bits de dados (LSB first), Stop bit (1)
        port.setBaudRate(5);
        port.setNumDataBits(8);
        port.setParity(SerialPort.NO_PARITY);

        // Enviar o byte de endereço
        out.write(address & 0xFF);
        out.flush();

        // Restaurar para velocidade normal
        Thread.sleep(BIT_DELAY_MS * 11); // aguardar transmissão completa
        port.setBaudRate(baudRate);
        port.setNumDataBits(8);
        port.setParity(SerialPort.ODD_PARITY);

        // Aguardar resposta 0x55 (sync byte) da ECU
        int syncByte = readByteWithTimeout(1500);
        if (syncByte != 0x55) {
            throw new IOException(String.format("Sync byte inválido: 0x%02X (esperado 0x55)", syncByte));
        }

        // Ler Keywords (2 bytes)
        int kw1 = readByteWithTimeout(1000);
        int kw2 = readByteWithTimeout(1000);
        System.out.printf("[K-LINE] Keywords: 0x%02X 0x%02X%n", kw1, kw2);

        // ACK invertido do último keyword
        Thread.sleep(25);
        out.write(~kw2 & 0xFF);
        out.flush();

        // Aguardar ACK da ECU (endereço invertido)
        int ack = readByteWithTimeout(1000);
        System.out.printf("[K-LINE] ACK da ECU: 0x%02X%n", ack);
    }

    /**
     * Envia um byte pela porta serial.
     */
    public void sendByte(int b) throws IOException {
        out.write(b & 0xFF);
        out.flush();
    }

    /**
     * Lê um byte com timeout.
     */
    public int readByteWithTimeout(int timeoutMs) throws IOException {
        long start = System.currentTimeMillis();
        while (System.currentTimeMillis() - start < timeoutMs) {
            if (in.available() > 0) {
                return in.read();
            }
            try { Thread.sleep(5); } catch (InterruptedException ignored) {}
        }
        throw new IOException("Timeout aguardando resposta da ECU (" + timeoutMs + "ms)");
    }

    /**
     * Lê múltiplos bytes.
     */
    public byte[] readBytes(int count, int timeoutMs) throws IOException {
        byte[] buffer = new byte[count];
        for (int i = 0; i < count; i++) {
            buffer[i] = (byte) readByteWithTimeout(timeoutMs);
        }
        return buffer;
    }

    public void close() {
        connected = false;
        if (port != null && port.isOpen()) {
            port.closePort();
        }
    }

    public boolean isConnected() { return connected && port != null && port.isOpen(); }
    public int getBaudRate()     { return baudRate; }
}
