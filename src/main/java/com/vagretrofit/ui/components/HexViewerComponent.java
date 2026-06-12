package com.vagretrofit.ui.components;

import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;

/**
 * Componente Hex Viewer com dark theme inline.
 * Usa TextFlow + ScrollPane para contornar problemas de CSS em TextArea.
 */
public class HexViewerComponent extends VBox {

    private static final String BG_COLOR       = "#0F1013";
    private static final String OFFSET_COLOR   = "#5F7A9B";
    private static final String HEX_COLOR      = "#B8C4CE";
    private static final String HEX_ZERO_COLOR = "#3C4048";
    private static final String ASCII_COLOR    = "#4CAF50";
    private static final String FONT_FAMILY    = "'JetBrains Mono', 'Consolas', 'Courier New', monospace";
    private static final String FONT_SIZE      = "12px";

    private final TextFlow textFlow;
    private final ScrollPane scrollPane;

    public HexViewerComponent() {
        textFlow = new TextFlow();
        textFlow.setStyle(
            "-fx-background-color: " + BG_COLOR + ";" +
            "-fx-padding: 10px;"
        );
        textFlow.setMaxWidth(Double.MAX_VALUE);

        scrollPane = new ScrollPane(textFlow);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle(
            "-fx-background: " + BG_COLOR + ";" +
            "-fx-background-color: " + BG_COLOR + ";" +
            "-fx-border-color: #2A2D35;" +
            "-fx-border-width: 1px;"
        );

        // Remover borda branca do viewport do ScrollPane
        scrollPane.skinProperty().addListener((obs, o, n) -> {
            scrollPane.lookup(".viewport").setStyle("-fx-background-color: " + BG_COLOR + ";");
        });

        VBox.setVgrow(scrollPane, javafx.scene.layout.Priority.ALWAYS);
        setStyle("-fx-background-color: " + BG_COLOR + ";");
        getChildren().add(scrollPane);
        setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);

        // Placeholder
        Text placeholder = styledText("  Nenhum dump carregado. Carregar na aba Patch EEPROM.", OFFSET_COLOR);
        textFlow.getChildren().add(placeholder);
    }

    public void displayDump(byte[] data) {
        if (data == null) return;
        textFlow.getChildren().clear();

        for (int i = 0; i < data.length; i += 16) {
            // Offset
            textFlow.getChildren().add(styledText(String.format("%04X  ", i), OFFSET_COLOR));

            // Hex bytes
            for (int j = 0; j < 16; j++) {
                if (i + j < data.length) {
                    int b = data[i + j] & 0xFF;
                    String color = (b == 0x00) ? HEX_ZERO_COLOR : HEX_COLOR;
                    textFlow.getChildren().add(styledText(String.format("%02X ", b), color));
                } else {
                    textFlow.getChildren().add(styledText("   ", HEX_COLOR));
                }
                if (j == 7) textFlow.getChildren().add(styledText(" ", HEX_COLOR));
            }

            // Separator
            textFlow.getChildren().add(styledText(" |", OFFSET_COLOR));

            // ASCII
            for (int j = 0; j < 16; j++) {
                if (i + j < data.length) {
                    char c = (char) (data[i + j] & 0xFF);
                    String ch = (c >= 32 && c <= 126) ? String.valueOf(c) : ".";
                    String color = (c >= 32 && c <= 126) ? ASCII_COLOR : HEX_ZERO_COLOR;
                    textFlow.getChildren().add(styledText(ch, color));
                }
            }

            // Line break
            textFlow.getChildren().add(styledText("|\n", OFFSET_COLOR));
        }

        // Scroll to top
        scrollPane.setVvalue(0);
    }

    private Text styledText(String content, String color) {
        Text t = new Text(content);
        t.setStyle(
            "-fx-fill: " + color + ";" +
            "-fx-font-family: " + FONT_FAMILY + ";" +
            "-fx-font-size: " + FONT_SIZE + ";"
        );
        return t;
    }

    public void clear() {
        textFlow.getChildren().clear();
    }
}
