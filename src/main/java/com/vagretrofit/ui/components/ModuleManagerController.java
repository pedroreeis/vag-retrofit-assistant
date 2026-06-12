package com.vagretrofit.ui.components;

import com.vagretrofit.domain.Module;
import com.vagretrofit.domain.OperatorPatchEntry;
import com.vagretrofit.repository.KnowledgeBaseRepository;
import com.vagretrofit.repository.OperatorPatchRepository;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;

import java.util.List;
import java.util.Map;

/**
 * V2: Tela separada de Gerenciamento da KB (módulos + patches do operador).
 * VCDS-inspired: table view com filtro lateral por endereço de módulo.
 */
@SuppressWarnings({"unchecked", "deprecation"})
public class ModuleManagerController {

    private final KnowledgeBaseRepository kbRepo;
    private final OperatorPatchRepository opRepo;
    private final VBox root;

    public ModuleManagerController(KnowledgeBaseRepository kbRepo, OperatorPatchRepository opRepo) {
        this.kbRepo = kbRepo;
        this.opRepo = opRepo;
        this.root   = buildUI();
    }

    public VBox getRoot() { return root; }

    private VBox buildUI() {
        VBox outer = new VBox(0);
        outer.getStyleClass().add("tab-content");

        // ── Toolbar ──
        Label title = new Label("Base de Conhecimento — Módulos & Patches");
        title.getStyleClass().add("section-title");

        Button btnRefresh  = new Button("↻ Atualizar");
        Button btnAddPatch = new Button("+ Adicionar Patch do Operador");
        btnAddPatch.getStyleClass().add("btn-accent");

        HBox toolbar = new HBox(10, title, new Region(), btnRefresh, btnAddPatch);
        HBox.setHgrow(toolbar.getChildren().get(1), Priority.ALWAYS);
        toolbar.setPadding(new Insets(12, 16, 12, 16));
        toolbar.setAlignment(Pos.CENTER_LEFT);
        toolbar.getStyleClass().add("toolbar");

        // ── Split: sidebar filter + table ──
        SplitPane split = new SplitPane();
        split.setDividerPositions(0.22);
        VBox.setVgrow(split, Priority.ALWAYS);

        // Left: address filter list
        VBox filterPanel = new VBox(4);
        filterPanel.setPadding(new Insets(10));
        filterPanel.getStyleClass().add("filter-panel");
        Label filterLabel = new Label("FILTRAR POR ENDEREÇO");
        filterLabel.getStyleClass().add("sidebar-section");
        ListView<String> filterList = new ListView<>();
        filterList.getStyleClass().add("filter-list");
        VBox.setVgrow(filterList, Priority.ALWAYS);
        filterPanel.getChildren().addAll(filterLabel, filterList);

        // Right: module table + operator patches
        VBox rightPanel = new VBox(8);
        rightPanel.setPadding(new Insets(12));

        TableView<ModuleRow> moduleTable = buildModuleTable();
        VBox.setVgrow(moduleTable, Priority.ALWAYS);

        Label opLabel = new Label("Patches do Operador  ← badge [OP] indica origem manual");
        opLabel.getStyleClass().add("operator-label");

        TableView<OpPatchRow> opTable = buildOperatorPatchTable();
        opTable.setPrefHeight(200);

        rightPanel.getChildren().addAll(moduleTable, new Separator(), opLabel, opTable);
        split.getItems().addAll(filterPanel, rightPanel);

        outer.getChildren().addAll(toolbar, new Separator(), split);

        // ── Load data ──
        Runnable refresh = () -> {
            // Fill filter list
            Map<String, Long> addresses = kbRepo.findAllModuleAddressesWithCount();
            filterList.getItems().clear();
            filterList.getItems().add("Todos (" + addresses.values().stream().mapToLong(Long::longValue).sum() + ")");
            addresses.forEach((addr, cnt) -> {
                String desc = com.vagretrofit.domain.ModuleAddress.fromCode(addr) != null
                    ? com.vagretrofit.domain.ModuleAddress.fromCode(addr).getDescription()
                    : "Módulo";
                filterList.getItems().add(addr + "  —  " + desc + "  (" + cnt + ")");
            });

            // Fill module table
            List<Module> modules = kbRepo.findAllModules();
            moduleTable.getItems().setAll(modules.stream().map(m -> new ModuleRow(
                m.getModuleAddress() != null ? m.getModuleAddress() : "?",
                m.getPartNumber(),
                m.getSoftwareVersion() != null ? m.getSoftwareVersion() : m.getPartNumber(),
                m.getImmo() != null ? m.getImmo() : "—",
                m.getChecksumAlgorithm() != null ? m.getChecksumAlgorithm() : "UNKNOWN",
                m.getEepromType() != null ? m.getEepromType() : "—",
                m.isChecksumRequiresFlash() ? "⚠️ Requer FLASH" : "EEPROM"
            )).toList());

            // Fill operator patches
            List<OperatorPatchEntry> opPatches = opRepo.findAll();
            opTable.getItems().setAll(opPatches.stream().map(op -> new OpPatchRow(
                "[OP] " + op.getPatchName(),
                op.getModuleAddress(),
                op.getTargetRomId(),
                String.format("0x%04X", op.getAddressStart()),
                op.isVerified() ? "✅ Testado" : "⚠️ Não testado"
            )).toList());
        };

        btnRefresh.setOnAction(e -> refresh.run());

        // Filter by address
        filterList.setOnMouseClicked(e -> {
            String sel = filterList.getSelectionModel().getSelectedItem();
            if (sel == null || sel.startsWith("Todos")) {
                moduleTable.getItems().setAll(kbRepo.findAllModules().stream().map(m -> new ModuleRow(
                    m.getModuleAddress() != null ? m.getModuleAddress() : "?",
                    m.getPartNumber(),
                    m.getSoftwareVersion() != null ? m.getSoftwareVersion() : "—",
                    m.getImmo() != null ? m.getImmo() : "—",
                    m.getChecksumAlgorithm() != null ? m.getChecksumAlgorithm() : "UNKNOWN",
                    m.getEepromType() != null ? m.getEepromType() : "—",
                    m.isChecksumRequiresFlash() ? "⚠️ Requer FLASH" : "EEPROM"
                )).toList());
            } else {
                String addr = sel.split("  ")[0].trim();
                moduleTable.getItems().setAll(kbRepo.findModulesByAddress(addr).stream().map(m -> new ModuleRow(
                    addr, m.getPartNumber(),
                    m.getSoftwareVersion() != null ? m.getSoftwareVersion() : "—",
                    m.getImmo() != null ? m.getImmo() : "—",
                    m.getChecksumAlgorithm() != null ? m.getChecksumAlgorithm() : "UNKNOWN",
                    m.getEepromType() != null ? m.getEepromType() : "—",
                    m.isChecksumRequiresFlash() ? "⚠️ Requer FLASH" : "EEPROM"
                )).toList());
            }
        });

        // Add operator patch dialog
        btnAddPatch.setOnAction(e -> showAddOperatorPatchDialog(refresh));

        refresh.run();
        return outer;
    }

    private TableView<ModuleRow> buildModuleTable() {
        TableView<ModuleRow> table = new TableView<>();
        table.getStyleClass().add("kb-table");
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);

        table.getColumns().addAll(
            col("Addr", "address", 60),
            col("Part Number", "partNumber", 160),
            col("Software", "softwareVersion", 110),
            col("IMMO", "immo", 80),
            col("EEPROM", "eepromType", 80),
            col("Checksum", "checksumAlgorithm", 160),
            col("Validação", "checksumScope", 140)
        );
        return table;
    }

    private TableView<OpPatchRow> buildOperatorPatchTable() {
        TableView<OpPatchRow> table = new TableView<>();
        table.getStyleClass().addAll("kb-table", "operator-table");
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        table.setPlaceholder(new Label("Nenhum patch do operador cadastrado"));

        table.getColumns().addAll(
            col("Badge / Nome", "name", 180),
            col("Módulo", "address", 70),
            col("ROM ID", "romId", 80),
            col("Offset", "addressStart", 80),
            col("Status", "status", 120)
        );
        return table;
    }

    private <T> TableColumn<T, String> col(String header, String property, double prefWidth) {
        TableColumn<T, String> col = new TableColumn<>(header);
        col.setCellValueFactory(new PropertyValueFactory<>(property));
        col.setPrefWidth(prefWidth);
        return col;
    }

    private void showAddOperatorPatchDialog(Runnable onSaved) {
        Dialog<OperatorPatchEntry> dialog = new Dialog<>();
        dialog.setTitle("Adicionar Patch do Operador");
        dialog.setHeaderText("[OP] Novo Patch — o operador assume total responsabilidade");
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10); grid.setVgap(8);
        grid.setPadding(new Insets(16));

        TextField tfName    = new TextField(); tfName.setPromptText("Nome do patch");
        TextField tfAddr    = new TextField("17"); tfAddr.setPromptText("Endereço (17, 46...)");
        TextField tfRomId   = new TextField(); tfRomId.setPromptText("ROM ID alvo (ex: 00A4)");
        TextField tfStart   = new TextField(); tfStart.setPromptText("Offset início (ex: 0x4F4)");
        TextField tfEnd     = new TextField(); tfEnd.setPromptText("Offset fim (ex: 0x693)");
        TextArea  taHex     = new TextArea(); taHex.setPromptText("Hex data (bytes separados por espaço)");
        taHex.setPrefHeight(80);
        TextArea  taNotes   = new TextArea(); taNotes.setPromptText("Notas / observações");
        taNotes.setPrefHeight(60);
        CheckBox  cbVerif   = new CheckBox("Patch testado em hardware real");

        grid.add(new Label("Nome:"),    0, 0); grid.add(tfName,  1, 0);
        grid.add(new Label("Módulo:"),  0, 1); grid.add(tfAddr,  1, 1);
        grid.add(new Label("ROM ID:"),  0, 2); grid.add(tfRomId, 1, 2);
        grid.add(new Label("Offset início:"), 0, 3); grid.add(tfStart, 1, 3);
        grid.add(new Label("Offset fim:"),    0, 4); grid.add(tfEnd,   1, 4);
        grid.add(new Label("Hex Data:"), 0, 5); grid.add(taHex, 1, 5);
        grid.add(new Label("Notas:"),   0, 6); grid.add(taNotes, 1, 6);
        grid.add(cbVerif, 1, 7);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(btn -> {
            if (btn == ButtonType.OK) {
                try {
                    String hexStr = taHex.getText().trim().replace(" ", "");
                    byte[] patchData = new byte[hexStr.length() / 2];
                    for (int i = 0; i < patchData.length; i++) {
                        patchData[i] = (byte) Integer.parseInt(hexStr.substring(i * 2, i * 2 + 2), 16);
                    }

                    OperatorPatchEntry entry = new OperatorPatchEntry();
                    entry.setPatchName(tfName.getText());
                    entry.setModuleAddress(tfAddr.getText());
                    entry.setTargetRomId(tfRomId.getText().toUpperCase());
                    entry.setAddressStart(Integer.decode(tfStart.getText()));
                    entry.setAddressEnd(Integer.decode(tfEnd.getText()));
                    entry.setPatchData(patchData);
                    entry.setPatchDataHex(taHex.getText().trim().toUpperCase());
                    entry.setNotes(taNotes.getText());
                    entry.setVerified(cbVerif.isSelected());
                    return entry;
                } catch (Exception ex) {
                    new Alert(Alert.AlertType.ERROR, "Erro ao parsear dados: " + ex.getMessage()).showAndWait();
                    return null;
                }
            }
            return null;
        });

        dialog.showAndWait().ifPresent(entry -> {
            if (entry != null) {
                opRepo.save(entry);
                onSaved.run();
            }
        });
    }

    // ── Row DTOs ────────────────────────────────────────────────────────────

    public static class ModuleRow {
        private final String address, partNumber, softwareVersion, immo, checksumAlgorithm, eepromType, checksumScope;
        public ModuleRow(String a, String p, String s, String i, String c, String e, String cs) {
            address=a; partNumber=p; softwareVersion=s; immo=i; checksumAlgorithm=c; eepromType=e; checksumScope=cs;
        }
        public String getAddress()           { return address; }
        public String getPartNumber()        { return partNumber; }
        public String getSoftwareVersion()   { return softwareVersion; }
        public String getImmo()              { return immo; }
        public String getChecksumAlgorithm() { return checksumAlgorithm; }
        public String getEepromType()        { return eepromType; }
        public String getChecksumScope()     { return checksumScope; }
    }

    public static class OpPatchRow {
        private final String name, address, romId, addressStart, status;
        public OpPatchRow(String n, String a, String r, String as, String s) {
            name=n; address=a; romId=r; addressStart=as; status=s;
        }
        public String getName()         { return name; }
        public String getAddress()      { return address; }
        public String getRomId()        { return romId; }
        public String getAddressStart() { return addressStart; }
        public String getStatus()       { return status; }
    }
}
