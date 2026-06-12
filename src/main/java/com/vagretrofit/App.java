package com.vagretrofit;

import com.vagretrofit.domain.Dump;
import com.vagretrofit.domain.PatchVariant;
import com.vagretrofit.repository.DatabaseManager;
import com.vagretrofit.repository.KnowledgeBaseRepository;
import com.vagretrofit.repository.OperatorPatchRepository;
import com.vagretrofit.service.*;
import com.vagretrofit.service.checksum.ChecksumResult;
import com.vagretrofit.ui.components.ChecksumPanelController;
import com.vagretrofit.ui.components.DumpMetadataController;
import com.vagretrofit.ui.components.HexViewerComponent;
import com.vagretrofit.ui.components.KLineController;
import com.vagretrofit.ui.components.ModuleManagerController;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.io.File;
import java.nio.file.Files;
import java.util.List;

/**
 * VAG Retrofit Assistant — V2
 * UI redesenhada com inspiração no VCDS: sidebar navigation, status bar, dark
 * theme.
 */
public class App extends Application {

    private static final String APP_TITLE = "VAG Retrofit Assistant";
    private static final String APP_VERSION = "2.0.0";

    // Services
    private IdentificationService identificationService;
    private PatchService patchService;
    private DumpMetadataService metadataService;
    private ChecksumService checksumService;
    private KnowledgeBaseRepository kbRepository;
    private OperatorPatchRepository operatorPatchRepository;

    // State
    private Dump currentDump;
    private Dump modifiedDump;
    private Stage primaryStage;

    // Window drag state (custom chrome)
    private double dragOffsetX, dragOffsetY;
    private double savedX, savedY, savedW, savedH;
    private boolean isMaximized = false;

    // UI refs
    private Label statusLabel;
    private Label dumpInfoLabel;
    private TabPane mainTabPane;
    private Button btnMaximize; // botão maximize do chrome customizado

    // Patch tab controls
    private TextArea patchLogArea;
    private ComboBox<PatchVariant> comboPatchVariants;
    private Button btnApplyPatch;
    private Button btnSaveDump;
    private Button btnViewHex;
    private Button btnAddMetadata;
    private Label lblRomStatus;
    private Label lblChecksumStatus;
    private ProgressBar progressBar;

    @Override
    public void start(Stage primaryStage) {
        this.primaryStage = primaryStage;
        DatabaseManager.initialize();
        identificationService = new IdentificationService();
        patchService = new PatchService(new ValidationService());
        metadataService = new DumpMetadataService();
        checksumService = new ChecksumService();
        kbRepository = new KnowledgeBaseRepository();
        operatorPatchRepository = new OperatorPatchRepository();

        // Custom window chrome — UNDECORATED para controlar botões manualmente
        primaryStage.initStyle(StageStyle.UNDECORATED);
        primaryStage.setMinWidth(1100);
        primaryStage.setMinHeight(700);
        Platform.setImplicitExit(true);

        BorderPane root = new BorderPane();
        root.getStyleClass().add("root-pane");
        root.setStyle("-fx-border-color: #0063CC; -fx-border-width: 1px;"); // borda azul VAG

        // Top: header com botões de janela embutidos
        root.setTop(buildHeader());

        // Left: sidebar navigation (VCDS-style)
        root.setLeft(buildSidebar());

        // Center: tab content
        mainTabPane = buildMainTabPane();
        root.setCenter(mainTabPane);

        // Bottom: status bar + resize grip
        root.setBottom(buildStatusBar());

        Scene scene = new Scene(root, 1200, 750);
        scene.getStylesheets().add(getClass().getResource("/css/vagretrofit-v2.css").toExternalForm());

        // Resize grip via mouse no canto inferior direito
        scene.setOnMouseMoved(e -> {
            double gripSize = 12;
            if (e.getX() > scene.getWidth() - gripSize && e.getY() > scene.getHeight() - gripSize) {
                scene.setCursor(Cursor.SE_RESIZE);
            } else {
                scene.setCursor(Cursor.DEFAULT);
            }
        });

        final double[] resizeStart = new double[4]; // x, y, w, h
        final boolean[] resizing = { false };
        scene.setOnMousePressed(e -> {
            double gripSize = 12;
            if (e.getX() > scene.getWidth() - gripSize && e.getY() > scene.getHeight() - gripSize) {
                resizing[0] = true;
                resizeStart[0] = e.getScreenX();
                resizeStart[1] = e.getScreenY();
                resizeStart[2] = primaryStage.getWidth();
                resizeStart[3] = primaryStage.getHeight();
            }
        });
        scene.setOnMouseDragged(e -> {
            if (resizing[0]) {
                double newW = Math.max(1100, resizeStart[2] + (e.getScreenX() - resizeStart[0]));
                double newH = Math.max(700, resizeStart[3] + (e.getScreenY() - resizeStart[1]));
                primaryStage.setWidth(newW);
                primaryStage.setHeight(newH);
            }
        });
        scene.setOnMouseReleased(e -> resizing[0] = false);

        primaryStage.setScene(scene);
        primaryStage.show();

        // Abrir maximizado na primeira execução
        javafx.geometry.Rectangle2D screen = javafx.stage.Screen.getPrimary().getVisualBounds();
        savedX = (screen.getWidth() - 1200) / 2;
        savedY = (screen.getHeight() - 750) / 2;
        savedW = 1200;
        savedH = 750;
        primaryStage.setX(screen.getMinX());
        primaryStage.setY(screen.getMinY());
        primaryStage.setWidth(screen.getWidth());
        primaryStage.setHeight(screen.getHeight());
        isMaximized = true;
        if (btnMaximize != null) btnMaximize.setText("▣");

        setStatus("Pronto — carregue um dump EEPROM para iniciar.", false);
    }

    // ─── Header (custom window chrome) ──────────────────────────────────────

    private HBox buildHeader() {
        HBox header = new HBox();
        header.getStyleClass().add("header-bar");
        header.setAlignment(Pos.CENTER_LEFT);
        header.setPadding(new Insets(0, 0, 0, 16));
        header.setPrefHeight(44);

        Label appName = new Label(APP_TITLE);
        appName.getStyleClass().add("header-title");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Label version = new Label("v" + APP_VERSION);
        version.getStyleClass().add("header-version");

        // ── Botões de controle de janela ──────────────────────────────────────
        Button btnMinimize = new Button("–"); // minimizar
        btnMaximize = new Button("□"); // maximizar / restaurar
        Button btnClose = new Button("✕"); // fechar

        btnMinimize.getStyleClass().addAll("wm-btn", "wm-minimize");
        btnMaximize.getStyleClass().addAll("wm-btn", "wm-maximize");
        btnClose.getStyleClass().addAll("wm-btn", "wm-close");

        btnMinimize.setOnAction(e -> primaryStage.setIconified(true));

        btnMaximize.setOnAction(e -> {
            if (isMaximized) {
                // Restaurar
                primaryStage.setX(savedX);
                primaryStage.setY(savedY);
                primaryStage.setWidth(savedW);
                primaryStage.setHeight(savedH);
                btnMaximize.setText("□");
                isMaximized = false;
            } else {
                // Salvar estado atual e maximizar
                savedX = primaryStage.getX();
                savedY = primaryStage.getY();
                savedW = primaryStage.getWidth();
                savedH = primaryStage.getHeight();
                javafx.geometry.Rectangle2D screen = javafx.stage.Screen.getPrimary().getVisualBounds();
                primaryStage.setX(screen.getMinX());
                primaryStage.setY(screen.getMinY());
                primaryStage.setWidth(screen.getWidth());
                primaryStage.setHeight(screen.getHeight());
                btnMaximize.setText("▣");
                isMaximized = true;
            }
        });

        btnClose.setOnAction(e -> Platform.exit());

        // Duplo clique no header para maximizar/restaurar
        header.setOnMouseClicked(e -> {
            if (e.getClickCount() == 2)
                btnMaximize.fire();
        });

        // Arrastar o header para mover a janela
        header.setOnMousePressed(e -> {
            dragOffsetX = e.getScreenX() - primaryStage.getX();
            dragOffsetY = e.getScreenY() - primaryStage.getY();
        });
        header.setOnMouseDragged(e -> {
            if (!isMaximized) {
                primaryStage.setX(e.getScreenX() - dragOffsetX);
                primaryStage.setY(e.getScreenY() - dragOffsetY);
            }
        });

        HBox wmButtons = new HBox(0, btnMinimize, btnMaximize, btnClose);
        wmButtons.setAlignment(Pos.CENTER_RIGHT);

        header.getChildren().addAll(appName, spacer, version, wmButtons);
        return header;
    }

    // ─── Sidebar ────────────────────────────────────────────────────────────

    private VBox buildSidebar() {
        VBox sidebar = new VBox(4);
        sidebar.getStyleClass().add("sidebar");
        sidebar.setPadding(new Insets(16, 0, 16, 0));
        sidebar.setPrefWidth(210);

        Label sectionLabel = new Label("OPERAÇÕES");
        sectionLabel.getStyleClass().add("sidebar-section");

        Button btnPatch = buildSidebarBtn("⚡  Patch EEPROM", 0);
        Button btnChecksum = buildSidebarBtn("🔐  Checksum", 1);
        Button btnKLine = buildSidebarBtn("🔌  K-Line", 2);
        Button btnHex = buildSidebarBtn("🔎  Hex Viewer", 3);

        Label sectionLabel2 = new Label("GERENCIAMENTO");
        sectionLabel2.getStyleClass().add("sidebar-section");
        sectionLabel2.setPadding(new Insets(16, 0, 4, 0));

        Button btnKB = buildSidebarBtn("📋  Base de Conhecimento", 4);
        Button btnAudit = buildSidebarBtn("📝  Auditoria", 5);

        sidebar.getChildren().addAll(
                sectionLabel, btnPatch, btnChecksum, btnKLine, btnHex,
                sectionLabel2, btnKB, btnAudit);
        return sidebar;
    }

    private Button buildSidebarBtn(String text, int tabIndex) {
        Button btn = new Button(text);
        btn.getStyleClass().add("sidebar-btn");
        btn.setMaxWidth(Double.MAX_VALUE);
        btn.setOnAction(e -> mainTabPane.getSelectionModel().select(tabIndex));
        return btn;
    }

    // ─── Main Tab Pane ───────────────────────────────────────────────────────

    private TabPane buildMainTabPane() {
        TabPane pane = new TabPane();
        pane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        pane.getStyleClass().add("main-tab-pane");

        pane.getTabs().addAll(
                buildPatchTab(),
                buildChecksumTab(),
                buildKLineTab(),
                buildHexViewerTab(),
                buildKBManagerTab(),
                buildAuditTab());
        return pane;
    }

    // ─── Tab 0: Patch EEPROM ────────────────────────────────────────────────

    private Tab buildPatchTab() {
        Tab tab = new Tab("Patch EEPROM");
        tab.setClosable(false);

        // ── Top info panel ──
        GridPane infoGrid = new GridPane();
        infoGrid.getStyleClass().add("info-grid");
        infoGrid.setHgap(20);
        infoGrid.setVgap(8);
        infoGrid.setPadding(new Insets(16));

        lblRomStatus = new Label("Nenhum dump carregado");
        lblRomStatus.getStyleClass().add("status-badge-neutral");

        lblChecksumStatus = new Label("—");
        lblChecksumStatus.getStyleClass().add("status-badge-neutral");

        dumpInfoLabel = new Label("—");
        dumpInfoLabel.getStyleClass().add("info-value");

        infoGrid.add(new Label("Dump:"), 0, 0);
        infoGrid.add(dumpInfoLabel, 1, 0);
        infoGrid.add(new Label("ROM Status:"), 0, 1);
        infoGrid.add(lblRomStatus, 1, 1);
        infoGrid.add(new Label("Checksum:"), 0, 2);
        infoGrid.add(lblChecksumStatus, 1, 2);

        // ── Action toolbar ──
        Button btnLoad = new Button("📂 Carregar Dump");
        btnLoad.getStyleClass().addAll("btn-primary");

        comboPatchVariants = new ComboBox<>();
        comboPatchVariants.setPromptText("Selecione um Patch...");
        comboPatchVariants.setDisable(true);
        comboPatchVariants.setPrefWidth(280);
        comboPatchVariants.setConverter(new javafx.util.StringConverter<PatchVariant>() {
            @Override
            public String toString(PatchVariant v) {
                if (v == null)
                    return "";
                String source = v.getPatch().getAuthor() != null ? " [" + v.getPatch().getAuthor() + "]" : "";
                return v.getPatch().getName() + " (" + v.getSoftwareVersion() + ")" + source;
            }

            @Override
            public PatchVariant fromString(String s) {
                return null;
            }
        });

        btnApplyPatch = new Button("⚡ Aplicar Patch");
        btnApplyPatch.getStyleClass().add("btn-accent");
        btnApplyPatch.setDisable(true);

        btnSaveDump = new Button("💾 Salvar Dump");
        btnSaveDump.getStyleClass().add("btn-success");
        btnSaveDump.setDisable(true);

        btnViewHex = new Button("🔎 Ver Hex");
        btnViewHex.setDisable(true);

        btnAddMetadata = new Button("📋 Metadados");
        btnAddMetadata.setDisable(true);

        progressBar = new ProgressBar(0);
        progressBar.setVisible(false);
        progressBar.setPrefWidth(200);

        HBox toolbar = new HBox(10, btnLoad, new Separator(javafx.geometry.Orientation.VERTICAL),
                comboPatchVariants, btnApplyPatch, new Separator(javafx.geometry.Orientation.VERTICAL),
                btnSaveDump, btnViewHex, btnAddMetadata, progressBar);
        toolbar.getStyleClass().add("toolbar");
        toolbar.setPadding(new Insets(10, 16, 10, 16));
        toolbar.setAlignment(Pos.CENTER_LEFT);

        // ── Log area ──
        patchLogArea = new TextArea();
        patchLogArea.setEditable(false);
        patchLogArea.getStyleClass().add("log-area");
        patchLogArea.setWrapText(true);

        VBox content = new VBox(0, infoGrid, new Separator(), toolbar, new Separator(), patchLogArea);
        VBox.setVgrow(patchLogArea, Priority.ALWAYS);
        content.getStyleClass().add("tab-content");

        // ── Event handlers ──
        btnLoad.setOnAction(e -> handleLoadDump());
        btnApplyPatch.setOnAction(e -> handleApplyPatch());
        btnSaveDump.setOnAction(e -> handleSaveDump());
        btnViewHex.setOnAction(e -> handleViewHex());
        btnAddMetadata.setOnAction(e -> handleAddMetadata());

        tab.setContent(content);
        return tab;
    }

    // ─── Tab 1: Checksum ────────────────────────────────────────────────────

    private Tab buildChecksumTab() {
        Tab tab = new Tab("Checksum");
        tab.setClosable(false);
        ChecksumPanelController ctrl = new ChecksumPanelController(checksumService);
        tab.setContent(ctrl.getRoot());
        return tab;
    }

    // ─── Tab 2: K-Line ──────────────────────────────────────────────────────

    private Tab buildKLineTab() {
        Tab tab = new Tab("K-Line");
        tab.setClosable(false);
        KLineController ctrl = new KLineController();
        tab.setContent(ctrl.getRoot());
        return tab;
    }

    // ─── Tab 3: Hex Viewer ──────────────────────────────────────────────────

    private Tab buildHexViewerTab() {
        Tab tab = new Tab("Hex Viewer");
        tab.setClosable(false);

        VBox content = new VBox(10);
        content.setPadding(new Insets(16));

        HexViewerComponent viewer = new HexViewerComponent();
        Button btnRefresh = new Button("↻ Atualizar com Dump Atual");
        btnRefresh.getStyleClass().add("btn-primary");
        btnRefresh.setOnAction(e -> {
            if (currentDump != null) {
                viewer.displayDump(modifiedDump != null ? modifiedDump.getData() : currentDump.getData());
            }
        });

        content.getChildren().addAll(btnRefresh, viewer);
        VBox.setVgrow(viewer, Priority.ALWAYS);

        tab.setContent(content);
        return tab;
    }

    // ─── Tab 4: KB Manager ──────────────────────────────────────────────────

    private Tab buildKBManagerTab() {
        Tab tab = new Tab("Base de Conhecimento");
        tab.setClosable(false);
        ModuleManagerController ctrl = new ModuleManagerController(kbRepository, operatorPatchRepository);
        tab.setContent(ctrl.getRoot());
        return tab;
    }

    // ─── Tab 5: Auditoria ───────────────────────────────────────────────────

    private Tab buildAuditTab() {
        Tab tab = new Tab("Auditoria");
        tab.setClosable(false);

        VBox content = new VBox(10);
        content.setPadding(new Insets(16));

        Label info = new Label("📋 Log de Auditoria — pasta: " + DatabaseManager.getAppDataDir() + "\\audit\\");
        info.getStyleClass().add("info-label");

        Button btnOpenDir = new Button("📂 Abrir Pasta de Auditoria");
        btnOpenDir.setOnAction(e -> {
            try {
                Runtime.getRuntime().exec("explorer " + DatabaseManager.getAppDataDir() + "\\audit");
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });

        content.getChildren().addAll(info, btnOpenDir);
        tab.setContent(content);
        return tab;
    }

    // ─── Status Bar ─────────────────────────────────────────────────────────

    private HBox buildStatusBar() {
        HBox bar = new HBox(16);
        bar.getStyleClass().add("status-bar");
        bar.setPadding(new Insets(6, 16, 6, 16));
        bar.setAlignment(Pos.CENTER_LEFT);

        Label indicator = new Label("●");
        indicator.setStyle("-fx-text-fill: #4CAF50;");

        statusLabel = new Label("Pronto");
        statusLabel.getStyleClass().add("status-text");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Label versionTag = new Label("VAG Retrofit Assistant  v" + APP_VERSION + "  ·  KB 2.0");
        versionTag.getStyleClass().add("status-version");

        bar.getChildren().addAll(indicator, statusLabel, spacer, versionTag);
        return bar;
    }

    private void setStatus(String msg, boolean isError) {
        Platform.runLater(() -> {
            statusLabel.setText(msg);
            statusLabel.setStyle(isError ? "-fx-text-fill: #FF5252;" : "-fx-text-fill: #E0E0E0;");
        });
    }

    // ─── Event Handlers ─────────────────────────────────────────────────────

    private void handleLoadDump() {
        FileChooser fc = new FileChooser();
        fc.setTitle("Abrir Dump EEPROM");
        fc.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("EEPROM Dumps (*.bin, *.eep)", "*.bin", "*.eep"),
                new FileChooser.ExtensionFilter("Todos os Arquivos (*.*)", "*.*"));
        File file = fc.showOpenDialog(primaryStage);
        if (file == null)
            return;

        try {
            byte[] data = Files.readAllBytes(file.toPath());
            currentDump = new Dump(data, file.getName());
            modifiedDump = null;
            dumpInfoLabel.setText(file.getName() + "  (" + data.length + " bytes)");
            appendLog("📂 Dump carregado: " + file.getName() + " (" + data.length + " bytes)");

            try {
                identificationService.identify(currentDump);
                var romVersion = currentDump.getIdentifiedRomVersion();
                var module = romVersion.getModule();

                appendLog("✅ Identificado: " + romVersion.getRomId() + " · " + module.getSoftwareVersion() +
                        " · " + module.getImmo() + " · addr=" + module.getModuleAddress());

                lblRomStatus.setText("✅ " + romVersion.getRomId() + " — " + module.getSoftwareVersion());
                lblRomStatus.getStyleClass().setAll("status-badge-success");

                // Checksum check
                ChecksumResult checksumResult = checksumService.validate(data, module);
                updateChecksumBadge(checksumResult);
                appendLog("[CHECKSUM] " + checksumResult.toString());

                // Carregar patches
                List<PatchVariant> patches = kbRepository.findPatchesForRomVersion(romVersion);
                comboPatchVariants.getItems().setAll(patches);
                if (!patches.isEmpty()) {
                    comboPatchVariants.getSelectionModel().selectFirst();
                    comboPatchVariants.setDisable(false);
                    btnApplyPatch.setDisable(false);
                } else {
                    comboPatchVariants.setDisable(true);
                    btnApplyPatch.setDisable(true);
                    appendLog("⚠️ Nenhum patch disponível na KB para esta ROM ID.");
                }

                btnViewHex.setDisable(false);
                btnAddMetadata.setDisable(false);
                setStatus("Dump identificado: " + romVersion.getRomId(), false);

            } catch (Exception ex) {
                appendLog("⛔ Erro de identificação: " + ex.getMessage());
                lblRomStatus.setText("⛔ Não identificado");
                lblRomStatus.getStyleClass().setAll("status-badge-error");
                comboPatchVariants.setDisable(true);
                btnApplyPatch.setDisable(true);
                btnViewHex.setDisable(false);
                setStatus("ROM ID não encontrado na KB.", true);
            }
        } catch (Exception ex) {
            appendLog("⛔ Erro ao ler arquivo: " + ex.getMessage());
            setStatus("Erro ao carregar dump.", true);
        }
    }

    private void handleApplyPatch() {
        PatchVariant selected = comboPatchVariants.getSelectionModel().getSelectedItem();
        if (currentDump == null || selected == null)
            return;

        appendLog("\n⚡ Aplicando patch: " + selected.getPatch().getName() + "...");
        progressBar.setVisible(true);
        progressBar.setProgress(ProgressBar.INDETERMINATE_PROGRESS);
        btnApplyPatch.setDisable(true);

        new Thread(() -> {
            try {
                modifiedDump = patchService.applyPatch(currentDump, selected);
                Platform.runLater(() -> {
                    appendLog("✅ Patch aplicado com sucesso!");
                    appendLog("   Hash antes: " + currentDump.getHashSha256().substring(0, 12) + "...");
                    appendLog("   Hash depois: " + modifiedDump.getHashSha256().substring(0, 12) + "...");
                    btnSaveDump.setDisable(false);
                    progressBar.setVisible(false);
                    btnApplyPatch.setDisable(false);
                    setStatus("Patch aplicado — pronto para salvar.", false);
                });
            } catch (Exception ex) {
                Platform.runLater(() -> {
                    appendLog("⛔ FALHA: " + ex.getMessage());
                    progressBar.setVisible(false);
                    btnApplyPatch.setDisable(false);
                    setStatus("Falha ao aplicar patch.", true);
                });
            }
        }).start();
    }

    private void handleSaveDump() {
        if (modifiedDump == null)
            return;
        FileChooser fc = new FileChooser();
        fc.setTitle("Salvar Dump Modificado");
        fc.setInitialFileName("PATCHED_" + currentDump.getFilename());
        File file = fc.showSaveDialog(primaryStage);
        if (file != null) {
            try {
                Files.write(file.toPath(), modifiedDump.getData());
                appendLog("✅ Dump salvo em: " + file.getAbsolutePath());
                setStatus("Dump salvo: " + file.getName(), false);
            } catch (Exception ex) {
                appendLog("⛔ Erro ao salvar: " + ex.getMessage());
                setStatus("Erro ao salvar dump.", true);
            }
        }
    }

    private void handleViewHex() {
        if (currentDump == null)
            return;
        Stage hexStage = new Stage();
        hexStage.setTitle("Hex Viewer — " + currentDump.getFilename());
        HexViewerComponent viewer = new HexViewerComponent();
        viewer.displayDump(modifiedDump != null ? modifiedDump.getData() : currentDump.getData());
        Scene scene = new Scene(viewer, 900, 650);
        if (getClass().getResource("/css/vagretrofit-v2.css") != null)
            scene.getStylesheets().add(getClass().getResource("/css/vagretrofit-v2.css").toExternalForm());
        hexStage.setScene(scene);
        hexStage.initModality(Modality.NONE);
        hexStage.show();
    }

    private void handleAddMetadata() {
        if (currentDump == null)
            return;
        Stage meta = new Stage();
        meta.setTitle("Metadados — " + currentDump.getFilename());
        String hash = currentDump.getHashSha256() != null ? currentDump.getHashSha256() : "HASH_NAO_CALCULADO";
        DumpMetadataController ctrl = new DumpMetadataController(metadataService, hash, currentDump.getFilename());
        Scene scene = new Scene(ctrl.getView(), 440, 340);
        meta.setScene(scene);
        meta.initModality(Modality.APPLICATION_MODAL);
        meta.showAndWait();
    }

    private void appendLog(String msg) {
        Platform.runLater(() -> {
            patchLogArea.appendText(msg + "\n");
        });
    }

    private void updateChecksumBadge(ChecksumResult result) {
        Platform.runLater(() -> {
            switch (result.getStatus()) {
                case VALID -> {
                    lblChecksumStatus.setText("✅ Válido — " + result.getEntries().size() + " checksum(s)");
                    lblChecksumStatus.getStyleClass().setAll("status-badge-success");
                }
                case INVALID -> {
                    lblChecksumStatus.setText("❌ Inválido — " + result.countInvalid() + " erro(s)");
                    lblChecksumStatus.getStyleClass().setAll("status-badge-error");
                }
                case REQUIRES_FLASH -> {
                    lblChecksumStatus.setText("⚠️ IMMO2 — Requer FLASH do MCU HC08");
                    lblChecksumStatus.getStyleClass().setAll("status-badge-warn");
                }
                default -> {
                    lblChecksumStatus.setText("⚠️ Algoritmo desconhecido");
                    lblChecksumStatus.getStyleClass().setAll("status-badge-warn");
                }
            }
        });
    }

    public static void main(String[] args) {
        launch(args);
    }
}
