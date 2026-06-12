package com.vagretrofit.ui.components;

import com.vagretrofit.service.ChecksumService;
import com.vagretrofit.service.checksum.ChecksumResult;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;

import java.io.File;
import java.nio.file.Files;

/**
 * V2: Painel de Checksum — carregar dump, validar, exibir resultado detalhado.
 */
public class ChecksumPanelController {

    private final ChecksumService checksumService;
    private final VBox root;

    public ChecksumPanelController(ChecksumService checksumService) {
        this.checksumService = checksumService;
        this.root = buildUI();
    }

    public VBox getRoot() { return root; }

    private VBox buildUI() {
        VBox outer = new VBox(0);
        outer.getStyleClass().add("tab-content");

        // ── Toolbar ──
        Label title = new Label("Checksum Calculator & Validation");
        title.getStyleClass().add("section-title");

        Button btnLoad = new Button("📂 Carregar Dump");
        btnLoad.getStyleClass().add("btn-primary");

        HBox toolbar = new HBox(10, title, new Region(), btnLoad);
        HBox.setHgrow(toolbar.getChildren().get(1), Priority.ALWAYS);
        toolbar.setPadding(new Insets(12, 16, 12, 16));
        toolbar.setAlignment(Pos.CENTER_LEFT);
        toolbar.getStyleClass().add("toolbar");

        // ── Status card ──
        Label statusTitle = new Label("Status de Checksum");
        statusTitle.getStyleClass().add("card-title");

        Label statusBadge = new Label("Carregue um dump para verificar");
        statusBadge.getStyleClass().add("status-badge-neutral");
        statusBadge.setStyle("-fx-font-size: 14px; -fx-padding: 8 16 8 16;");

        Label algorithmLabel = new Label("Algoritmo: —");
        algorithmLabel.getStyleClass().add("info-label");

        TextArea detailArea = new TextArea();
        detailArea.setEditable(false);
        detailArea.setPromptText("Detalhes dos checksums aparecerão aqui...");
        detailArea.getStyleClass().add("log-area");
        detailArea.setPrefHeight(250);

        VBox statusCard = new VBox(12, statusTitle, statusBadge, algorithmLabel, detailArea);
        statusCard.getStyleClass().add("card");
        statusCard.setPadding(new Insets(16));

        // ── Info panel ──
        Label infoTitle = new Label("ℹ️ Sobre o Checksum VDO PQ34");
        infoTitle.getStyleClass().add("card-title");

        String infoText =
            "• IMMO3 (VWK501MH / VWK503MH): Validação baseada em padrões conhecidos dos patch variants.\n" +
            "  Os 2 primeiros bytes em 0x4F4 identificam o tipo de patch aplicado.\n\n" +
            "• IMMO2 (ex: 1J0 920 825 A): A rotina de checksum está na FLASH do MCU Motorola HC08,\n" +
            "  não na EEPROM externa. Validação completa requer engenharia reversa do firmware.\n\n" +
            "• Política zero-tolerância: Qualquer resultado que não seja VÁLIDO bloqueia\n" +
            "  a aplicação de patch. Isso previne erro dEF, bootloop e brick do painel.\n\n" +
            "• Os patches catalogados na KB (Graeme's Webspace / hayperek.pl) já incluem\n" +
            "  os bytes de checksum corretos na sequência hexadecimal.";

        TextArea infoArea = new TextArea(infoText);
        infoArea.setEditable(false);
        infoArea.setWrapText(true);
        infoArea.getStyleClass().add("info-area");
        infoArea.setPrefHeight(160);

        VBox infoCard = new VBox(8, infoTitle, infoArea);
        infoCard.getStyleClass().add("card");
        infoCard.setPadding(new Insets(16));

        VBox content = new VBox(12, statusCard, infoCard);
        content.setPadding(new Insets(16));
        VBox.setVgrow(content, Priority.ALWAYS);

        outer.getChildren().addAll(toolbar, new Separator(), content);

        // ── Load action ──
        btnLoad.setOnAction(e -> {
            FileChooser fc = new FileChooser();
            fc.setTitle("Abrir Dump EEPROM para Validação de Checksum");
            fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("Dumps (*.bin,*.eep)", "*.bin", "*.eep"));
            File file = fc.showOpenDialog(null);
            if (file == null) return;

            try {
                byte[] data = Files.readAllBytes(file.toPath());
                // Para a aba de checksum, usamos um module mock básico para IMMO3
                // (validação plena requer identificação completa do dump)
                com.vagretrofit.domain.Module mockModule = new com.vagretrofit.domain.Module();
                mockModule.setChecksumAlgorithm("VDO_PQ34_IMMO3");
                mockModule.setChecksumRequiresFlash(false);

                ChecksumResult result = checksumService.validate(data, mockModule);
                algorithmLabel.setText("Algoritmo: " + checksumService.getAlgorithmName(mockModule));

                switch (result.getStatus()) {
                    case VALID -> {
                        statusBadge.setText("✅ CHECKSUM VÁLIDO");
                        statusBadge.getStyleClass().setAll("status-badge-success");
                        statusBadge.setStyle("-fx-font-size: 14px; -fx-padding: 8 16 8 16;");
                    }
                    case INVALID -> {
                        statusBadge.setText("❌ CHECKSUM INVÁLIDO — " + result.countInvalid() + " erro(s)");
                        statusBadge.getStyleClass().setAll("status-badge-error");
                        statusBadge.setStyle("-fx-font-size: 14px; -fx-padding: 8 16 8 16;");
                    }
                    case REQUIRES_FLASH -> {
                        statusBadge.setText("⚠️ REQUER FLASH MCU HC08");
                        statusBadge.getStyleClass().setAll("status-badge-warn");
                        statusBadge.setStyle("-fx-font-size: 14px; -fx-padding: 8 16 8 16;");
                    }
                    default -> {
                        statusBadge.setText("⚠️ ALGORITMO DESCONHECIDO");
                        statusBadge.getStyleClass().setAll("status-badge-warn");
                        statusBadge.setStyle("-fx-font-size: 14px; -fx-padding: 8 16 8 16;");
                    }
                }

                StringBuilder detail = new StringBuilder();
                detail.append("Arquivo: ").append(file.getName())
                      .append("  (").append(data.length).append(" bytes)\n\n");
                detail.append("Resultado: ").append(result.getStatus()).append("\n");
                detail.append("Mensagem: ").append(result.getMessage()).append("\n\n");
                detail.append("Detalhes por checksum:\n");
                for (ChecksumResult.ChecksumEntry entry : result.getEntries()) {
                    detail.append("  ").append(entry.toString()).append("\n");
                }
                detailArea.setText(detail.toString());

            } catch (Exception ex) {
                statusBadge.setText("⛔ Erro ao ler dump");
                statusBadge.getStyleClass().setAll("status-badge-error");
                detailArea.setText("Erro: " + ex.getMessage());
            }
        });

        return outer;
    }
}
