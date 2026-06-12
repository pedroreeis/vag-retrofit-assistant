package com.vagretrofit.ui.components;

import com.vagretrofit.service.DumpMetadataService;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;

public class DumpMetadataController {

    private final DumpMetadataService metadataService;
    private final String currentHash;
    private final String currentFilename;

    public DumpMetadataController(DumpMetadataService metadataService, String hash, String filename) {
        this.metadataService = metadataService;
        this.currentHash = hash;
        this.currentFilename = filename;
    }

    public VBox getView() {
        VBox root = new VBox(10);
        root.setPadding(new Insets(15));

        Label title = new Label("Registrar Metadados Extraídos (VAG EEPROM Programmer)");
        title.setStyle("-fx-font-weight: bold;");

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);

        TextField txtVin = new TextField();
        TextField txtPin = new TextField();
        TextField txtMileage = new TextField();
        TextField txtImmoStatus = new TextField();
        TextField txtSoftware = new TextField("VAG EEPROM Programmer 1.19g");

        grid.add(new Label("VIN:"), 0, 0);
        grid.add(txtVin, 1, 0);
        grid.add(new Label("PIN / SKC:"), 0, 1);
        grid.add(txtPin, 1, 1);
        grid.add(new Label("Quilometragem (km):"), 0, 2);
        grid.add(txtMileage, 1, 2);
        grid.add(new Label("Status IMMO:"), 0, 3);
        grid.add(txtImmoStatus, 1, 3);
        grid.add(new Label("Software Usado na Extração:"), 0, 4);
        grid.add(txtSoftware, 1, 4);

        Button btnSave = new Button("Salvar no Banco de Dados");
        Label lblFeedback = new Label();
        lblFeedback.setStyle("-fx-text-fill: green;");

        btnSave.setOnAction(e -> {
            try {
                Integer mileage = txtMileage.getText().isEmpty() ? null : Integer.parseInt(txtMileage.getText());
                
                metadataService.registerExternalMetadata(
                    currentHash,
                    currentFilename,
                    txtVin.getText(),
                    txtPin.getText(),
                    null,
                    mileage,
                    txtImmoStatus.getText(),
                    txtSoftware.getText()
                );
                
                lblFeedback.setText("Salvo com sucesso!");
                btnSave.setDisable(true);
            } catch (Exception ex) {
                lblFeedback.setStyle("-fx-text-fill: red;");
                lblFeedback.setText("Erro: " + ex.getMessage());
            }
        });

        root.getChildren().addAll(title, grid, btnSave, lblFeedback);
        return root;
    }
}
