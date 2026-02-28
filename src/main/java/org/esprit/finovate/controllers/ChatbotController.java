package org.esprit.finovate.controllers;

import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import org.esprit.finovate.services.ChatbotService;
import org.esprit.finovate.services.GroqChatbotService;

import java.net.URL;
import java.util.ResourceBundle;

public class ChatbotController implements Initializable {

    @FXML
    private VBox messagesBox;

    @FXML
    private ScrollPane scrollPane;

    @FXML
    private TextField messageField;

    @FXML
    private Button sendButton;

    @FXML
    private Label statusLabel;

    private final ChatbotService chatbotService = new GroqChatbotService();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        if (statusLabel != null) {
            statusLabel.setText("Prêt");
        }

        if (messageField != null) {
            messageField.setOnAction(e -> handleSend());
        }

        // Auto-scroll when messagesBox height changes
        if (messagesBox != null && scrollPane != null) {
            messagesBox.heightProperty().addListener((obs, oldVal, newVal) -> {
                Platform.runLater(() -> {
                    scrollPane.setVvalue(1.0);
                });
            });
        }

        addSystemMessage("Bonjour, je suis Finovate AI. Pose-moi une question en comptabilité, finance ou banque.");
    }

    @FXML
    private void handleSend() {
        if (messageField == null || messagesBox == null) {
            return;
        }

        String userText = messageField.getText();
        if (userText == null || userText.trim().isEmpty()) {
            return;
        }

        messageField.clear();
        addUserMessage(userText.trim());

        setLoading(true);

        Task<String> task = new Task<>() {
            @Override
            protected String call() throws Exception {
                return chatbotService.ask(userText);
            }
        };

        task.setOnSucceeded(e -> {
            String answer = task.getValue();
            addBotMessage(answer);
            setLoading(false);
        });

        task.setOnFailed(e -> {
            Throwable ex = task.getException();
            addSystemMessage("Erreur: " + (ex == null ? "Unknown" : ex.getMessage()));
            setLoading(false);
        });

        Thread th = new Thread(task);
        th.setDaemon(true);
        th.start();
    }

    private void setLoading(boolean loading) {
        Platform.runLater(() -> {
            if (sendButton != null) {
                sendButton.setDisable(loading);
            }
            if (messageField != null) {
                messageField.setDisable(loading);
            }
            if (statusLabel != null) {
                statusLabel.setText(loading ? "Réponse en cours..." : "Prêt");
            }
        });
    }

    private void addUserMessage(String text) {
        messagesBox.getChildren().add(buildBubble(text, true));
        scrollToBottom();
    }

    private void addBotMessage(String text) {
        messagesBox.getChildren().add(buildBubble(text, false));
        scrollToBottom();
    }

    private void addSystemMessage(String text) {
        Label label = new Label(text);
        label.setWrapText(true);
        label.setStyle("-fx-text-fill: #6c757d; -fx-font-size: 12px;");
        VBox.setMargin(label, new Insets(6, 0, 6, 0));
        messagesBox.getChildren().add(label);
        scrollToBottom();
    }

    private HBox buildBubble(String text, boolean isUser) {
        Label label = new Label(text);
        label.setWrapText(true);
        label.setMaxWidth(650);
        label.setStyle(isUser
                ? "-fx-background-color: #237f4e; -fx-text-fill: white; -fx-padding: 10 12 10 12; -fx-background-radius: 12;"
                : "-fx-background-color: white; -fx-text-fill: #32325d; -fx-padding: 10 12 10 12; -fx-background-radius: 12; -fx-border-color: #e9ecef; -fx-border-radius: 12;");

        HBox row = new HBox();
        row.setFillHeight(true);
        HBox.setHgrow(label, Priority.NEVER);

        if (isUser) {
            row.setStyle("-fx-alignment: CENTER_RIGHT;");
        } else {
            row.setStyle("-fx-alignment: CENTER_LEFT;");
        }

        row.getChildren().add(label);
        VBox.setMargin(row, new Insets(2, 0, 2, 0));
        return row;
    }

    private void scrollToBottom() {
        if (scrollPane != null) {
            Platform.runLater(() -> {
                scrollPane.setVvalue(1.0);
            });
        }
    }
}
