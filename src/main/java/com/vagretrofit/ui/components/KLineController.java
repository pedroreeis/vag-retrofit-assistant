package com.vagretrofit.ui.components;

import com.fazecast.jSerialComm.SerialPort;
import com.vagretrofit.domain.ModuleAddress;
import com.vagretrofit.kline.KLineConnection;
import com.vagretrofit.service.KLineService;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;

import java.io.File;
import java.nio.file.Files;

/**
 * V2: Aba de Comunicação K-Line — DumpEeprom, ReadEeprom, WriteEeprom, GetSKC.
 * Inspirado no VCDS: port/address selector + operation log + progress.
 */
public class KLineController {

    private final KLineService klineService;
    private final VBox root;

    // UI refs
    private ComboBox<String> comboPort;
    private ComboBox<String> comboModule;
    private TextField tfBaud;
    private Button btnConnect;
    private Button btnDisconnect;
    private Button btnDump;
    private Button btnReadBytes;
    private Button btnWriteBytes;
    private Button btnGetSKC;
    private TextArea logArea;
    private ProgressBar progressBar;
    private Label connStatus;

    public KLineController() {
        this.klineService = new KLineService();
        this.root = buildUI();
        this.klineService.setLogCallback(msg -> Platform.runLater(() -> appendLog("[K-LINE] " + msg)));
    }

    public VBox getRoot() { return root; }

    private VBox buildUI() {
        VBox outer = new VBox(0);
        outer.getStyleClass().add("tab-content");

        // ── Toolbar ──
        Label title = new Label("Comunicação K-Line — kw1281test");
        title.getStyleClass().add("section-title");

        HBox toolbar = new HBox(10, title);
        toolbar.setPadding(new Insets(12, 16, 12, 16));
        toolbar.setAlignment(Pos.CENTER_LEFT);
        toolbar.getStyleClass().add("toolbar");

        outer.getChildren().addAll(toolbar, new Separator());

        // ── Split: left=config, right=log ──
        SplitPane split = new SplitPane();
        split.setDividerPositions(0.35);
        VBox.setVgrow(split, Priority.ALWAYS);

        // ── Left: connection panel ──
        VBox leftPanel = new VBox(12);
        leftPanel.setPadding(new Insets(16));
        leftPanel.getStyleClass().add("config-panel");

        // Connection status badge
        connStatus = new Label("⛔  Desconectado");
        connStatus.getStyleClass().add("status-badge-error");

        // Port selection
        comboPort = new ComboBox<>();
        comboPort.setMaxWidth(Double.MAX_VALUE);
        comboPort.setPromptText("Selecionar porta COM...");
        Button btnRefreshPorts = new Button("↻");
        btnRefreshPorts.setOnAction(e -> refreshPorts());
        HBox portRow = new HBox(6, comboPort, btnRefreshPorts);
        HBox.setHgrow(comboPort, Priority.ALWAYS);

        // Module selection
        comboModule = new ComboBox<>();
        comboModule.setMaxWidth(Double.MAX_VALUE);
        for (ModuleAddress addr : ModuleAddress.values()) {
            comboModule.getItems().add(addr.toString());
        }
        comboModule.getSelectionModel().select(
            ModuleAddress.INSTRUMENTS.toString()
        );

        // Baud rate
        tfBaud = new TextField(String.valueOf(KLineService.DEFAULT_BAUD_PUBLIC));
        tfBaud.setPromptText("Baud rate (padrão: 10400)");

        // Connect / Disconnect
        btnConnect    = new Button("🔌  Conectar");
        btnConnect.getStyleClass().add("btn-primary");
        btnConnect.setMaxWidth(Double.MAX_VALUE);
        btnDisconnect = new Button("⏏  Desconectar");
        btnDisconnect.getStyleClass().add("btn-danger");
        btnDisconnect.setMaxWidth(Double.MAX_VALUE);
        btnDisconnect.setDisable(true);

        Separator sep = new Separator();

        // ── Operations ──
        Label opLabel = new Label("OPERAÇÕES EEPROM");
        opLabel.getStyleClass().add("sidebar-section");

        btnDump = new Button("💾  DumpEeprom");
        btnDump.setMaxWidth(Double.MAX_VALUE);
        btnDump.setDisable(true);

        TextField tfDumpSize = new TextField("2048");
        tfDumpSize.setPromptText("Tamanho EEPROM (bytes)");

        btnReadBytes = new Button("📖  ReadEeprom (range)");
        btnReadBytes.setMaxWidth(Double.MAX_VALUE);
        btnReadBytes.setDisable(true);

        TextField tfReadOffset = new TextField("0x4F8");
        TextField tfReadLen    = new TextField("2");

        btnWriteBytes = new Button("✏️  WriteEeprom (range)");
        btnWriteBytes.setMaxWidth(Double.MAX_VALUE);
        btnWriteBytes.setDisable(true);

        TextField tfWriteOffset = new TextField("0x0000");
        TextArea  taWriteData   = new TextArea();
        taWriteData.setPromptText("Hex bytes para escrita (ex: FF 00 A5)");
        taWriteData.setPrefHeight(60);

        btnGetSKC = new Button("🔑  GetSKC (PIN)");
        btnGetSKC.getStyleClass().add("btn-accent");
        btnGetSKC.setMaxWidth(Double.MAX_VALUE);
        btnGetSKC.setDisable(true);

        progressBar = new ProgressBar(0);
        progressBar.setVisible(false);
        progressBar.setMaxWidth(Double.MAX_VALUE);

        leftPanel.getChildren().addAll(
            connStatus,
            new Label("Porta Serial:"), portRow,
            new Label("Módulo:"), comboModule,
            new Label("Baud Rate:"), tfBaud,
            btnConnect, btnDisconnect,
            sep, opLabel,
            new Label("Tamanho EEPROM (bytes):"), tfDumpSize, btnDump,
            new Label("Offset de leitura:"), tfReadOffset,
            new Label("Quantidade de bytes:"), tfReadLen, btnReadBytes,
            new Label("Offset de escrita:"), tfWriteOffset,
            new Label("Bytes (hex):"), taWriteData, btnWriteBytes,
            btnGetSKC, progressBar
        );

        // ── Right: log ──
        logArea = new TextArea();
        logArea.setEditable(false);
        logArea.getStyleClass().add("log-area");
        logArea.setWrapText(true);
        logArea.setPromptText("Log de operações K-Line aparecerá aqui...");

        split.getItems().addAll(leftPanel, logArea);
        outer.getChildren().add(split);

        // ── Event Handlers ──
        refreshPorts();

        btnRefreshPorts.setOnAction(e -> refreshPorts());

        btnConnect.setOnAction(e -> {
            String portSel = comboPort.getSelectionModel().getSelectedItem();
            if (portSel == null || portSel.isEmpty()) {
                appendLog("⛔ Selecione uma porta serial.");
                return;
            }
            // Extrai apenas o nome da porta (antes do " — ")
            String portName = portSel.split(" — ")[0].trim();
            String moduleSel = comboModule.getSelectionModel().getSelectedItem();
            int moduleAddr = 17;
            if (moduleSel != null) {
                String code = moduleSel.split(" — ")[0].trim();
                moduleAddr = Integer.parseInt(code);
            }
            int baud = 10400;
            try { baud = Integer.parseInt(tfBaud.getText().trim()); } catch (Exception ignored) {}

            final int finalAddr = moduleAddr;
            final int finalBaud = baud;
            final String finalPort = portName;
            setConnecting(true);

            new Thread(() -> {
                try {
                    klineService.connect(finalPort, finalAddr, finalBaud);
                    Platform.runLater(() -> {
                        connStatus.setText("✅  Conectado — " + finalPort + " → 0x" + String.format("%02X", finalAddr));
                        connStatus.getStyleClass().setAll("status-badge-success");
                        setConnected(true);
                        appendLog("✅ Conectado a " + finalPort + " módulo 0x" + String.format("%02X", finalAddr));
                    });
                } catch (Exception ex) {
                    Platform.runLater(() -> {
                        connStatus.setText("⛔  Erro de conexão");
                        connStatus.getStyleClass().setAll("status-badge-error");
                        setConnecting(false);
                        appendLog("⛔ Falha na conexão: " + ex.getMessage());
                    });
                }
            }).start();
        });

        btnDisconnect.setOnAction(e -> {
            klineService.disconnect();
            connStatus.setText("⛔  Desconectado");
            connStatus.getStyleClass().setAll("status-badge-error");
            setConnected(false);
            appendLog("Desconectado.");
        });

        btnDump.setOnAction(e -> {
            int size;
            try { size = Integer.parseInt(tfDumpSize.getText().trim()); }
            catch (Exception ex) { size = 2048; }
            final int finalSize = size;

            progressBar.setVisible(true);
            progressBar.setProgress(ProgressBar.INDETERMINATE_PROGRESS);
            btnDump.setDisable(true);
            appendLog("Iniciando DumpEeprom (" + finalSize + " bytes)...");

            new Thread(() -> {
                try {
                    byte[] data = klineService.dumpEeprom(finalSize, "module");
                    final byte[] finalData = data;
                    Platform.runLater(() -> {
                        progressBar.setVisible(false);
                        btnDump.setDisable(false);
                        appendLog("✅ Dump concluído — " + finalData.length + " bytes lidos.");

                        // Perguntar onde salvar
                        FileChooser fc = new FileChooser();
                        fc.setTitle("Salvar Dump EEPROM");
                        fc.setInitialFileName("kline_dump.bin");
                        fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("Binário (*.bin)", "*.bin"));
                        File file = fc.showSaveDialog(null);
                        if (file != null) {
                            try {
                                Files.write(file.toPath(), finalData);
                                appendLog("✅ Salvo em: " + file.getAbsolutePath());
                            } catch (Exception ex) {
                                appendLog("⛔ Erro ao salvar: " + ex.getMessage());
                            }
                        }
                    });
                } catch (Exception ex) {
                    Platform.runLater(() -> {
                        progressBar.setVisible(false);
                        btnDump.setDisable(false);
                        appendLog("⛔ Erro no DumpEeprom: " + ex.getMessage());
                    });
                }
            }).start();
        });

        btnReadBytes.setOnAction(e -> {
            try {
                int offset = Integer.decode(tfReadOffset.getText().trim());
                int len    = Integer.parseInt(tfReadLen.getText().trim());
                new Thread(() -> {
                    try {
                        byte[] data = klineService.readEeprom(offset, len, "module");
                        StringBuilder sb = new StringBuilder();
                        for (byte b : data) sb.append(String.format("%02X ", b));
                        Platform.runLater(() -> appendLog(String.format("ReadEeprom 0x%04X +%d: %s", offset, len, sb.toString().trim())));
                    } catch (Exception ex) {
                        Platform.runLater(() -> appendLog("⛔ ReadEeprom: " + ex.getMessage()));
                    }
                }).start();
            } catch (Exception ex) {
                appendLog("⛔ Parâmetros inválidos: " + ex.getMessage());
            }
        });

        btnWriteBytes.setOnAction(e -> {
            try {
                int offset = Integer.decode(tfWriteOffset.getText().trim());
                String[] hexTokens = taWriteData.getText().trim().split("\\s+");
                byte[] writeData = new byte[hexTokens.length];
                for (int i = 0; i < hexTokens.length; i++) {
                    writeData[i] = (byte) Integer.parseInt(hexTokens[i], 16);
                }
                final byte[] finalData = writeData;

                Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                    String.format("ATENÇÃO: Você está prestes a ESCREVER %d bytes na EEPROM em 0x%04X.\n" +
                                  "Esta operação é irreversível sem um backup.\n\nConfirmar?", finalData.length, offset),
                    ButtonType.YES, ButtonType.NO);
                confirm.setTitle("Confirmação de Escrita EEPROM");
                confirm.showAndWait().ifPresent(btn -> {
                    if (btn == ButtonType.YES) {
                        new Thread(() -> {
                            try {
                                klineService.writeEeprom(offset, finalData, "module");
                                Platform.runLater(() -> appendLog(
                                    String.format("✅ WriteEeprom 0x%04X: %d bytes escritos.", offset, finalData.length)));
                            } catch (Exception ex) {
                                Platform.runLater(() -> appendLog("⛔ WriteEeprom: " + ex.getMessage()));
                            }
                        }).start();
                    }
                });
            } catch (Exception ex) {
                appendLog("⛔ Parâmetros inválidos: " + ex.getMessage());
            }
        });

        btnGetSKC.setOnAction(e -> {
            new Thread(() -> {
                try {
                    String skc = klineService.getSKC("module");
                    Platform.runLater(() -> {
                        if (skc != null) {
                            appendLog("🔑 SKC (PIN): " + skc);
                            new Alert(Alert.AlertType.INFORMATION,
                                "Security Key Code (SKC / PIN):\n\n" + skc + "\n\nAnote este valor com segurança.")
                                .showAndWait();
                        } else {
                            appendLog("⚠️ SKC não disponível para este módulo.");
                        }
                    });
                } catch (Exception ex) {
                    Platform.runLater(() -> appendLog("⛔ GetSKC: " + ex.getMessage()));
                }
            }).start();
        });

        return outer;
    }

    private void refreshPorts() {
        comboPort.getItems().clear();
        SerialPort[] ports = KLineConnection.getCommPorts();
        for (SerialPort p : ports) {
            comboPort.getItems().add(p.getSystemPortName() + " — " + p.getDescriptivePortName());
        }
        if (comboPort.getItems().isEmpty()) {
            comboPort.setPromptText("Nenhuma porta encontrada");
        } else {
            comboPort.getSelectionModel().selectFirst();
        }
        appendLog("Portas disponíveis: " + comboPort.getItems().size());
    }

    private void setConnecting(boolean connecting) {
        btnConnect.setDisable(connecting);
    }

    private void setConnected(boolean connected) {
        btnConnect.setDisable(connected);
        btnDisconnect.setDisable(!connected);
        btnDump.setDisable(!connected);
        btnReadBytes.setDisable(!connected);
        btnWriteBytes.setDisable(!connected);
        btnGetSKC.setDisable(!connected);
    }

    private void appendLog(String msg) {
        Platform.runLater(() -> logArea.appendText(msg + "\n"));
    }

    // Public constant to avoid code duplication
    public static final int DEFAULT_BAUD_PUBLIC = 10400;
}
