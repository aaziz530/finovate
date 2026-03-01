package org.esprit.finovate.controllers;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import org.esprit.finovate.services.AIAssistantService;

import java.util.ArrayList;
import java.util.List;

public class AIAssistantController {

    @FXML private ScrollPane chatScrollPane;
    @FXML private VBox chatContainer;
    @FXML private TextField messageField;
    @FXML private Button sendButton;

    private MainController mainController;
    private List<AIAssistantService.Message> conversationHistory = new ArrayList<>();

    public void setMainController(MainController mainController) {
        this.mainController = mainController;
    }

    @FXML
    public void initialize() {
        // Auto-scroll vers le bas
        chatContainer.heightProperty().addListener((obs, oldVal, newVal) -> {
            chatScrollPane.setVvalue(1.0);
        });
        
        // Envoyer avec Enter
        messageField.setOnAction(e -> sendMessage());
        
        // Message de bienvenue
        addAssistantMessage("👋 Bonjour ! Je suis votre assistant IA. Comment puis-je vous aider aujourd'hui ?");
    }

    @FXML
    private void sendMessage() {
        String userMessage = messageField.getText().trim();
        if (userMessage.isEmpty()) return;
        
        // Afficher le message utilisateur
        addUserMessage(userMessage);
        messageField.clear();
        
        // Ajouter à l'historique
        conversationHistory.add(new AIAssistantService.Message("user", userMessage));
        
        // Afficher "en train d'écrire..."
        Label typingLabel = new Label("🤖 Assistant IA est en train d'écrire...");
        typingLabel.setStyle("-fx-text-fill: #999; -fx-font-style: italic; -fx-padding: 10;");
        chatContainer.getChildren().add(typingLabel);
        
        // Appeler l'IA en arrière-plan
        new Thread(() -> {
            String response = AIAssistantService.chat(userMessage, conversationHistory);
            
            Platform.runLater(() -> {
                // Retirer "en train d'écrire..."
                chatContainer.getChildren().remove(typingLabel);
                
                // Afficher la réponse
                addAssistantMessage(response);
                
                // Ajouter à l'historique
                conversationHistory.add(new AIAssistantService.Message("assistant", response));
            });
        }).start();
    }

    private void addUserMessage(String message) {
        HBox messageBox = new HBox(10);
        messageBox.setAlignment(Pos.CENTER_RIGHT);
        messageBox.setPadding(new Insets(5, 10, 5, 50));
        
        VBox bubble = new VBox(5);
        bubble.setStyle(
            "-fx-background-color: #0079D3; " +
            "-fx-background-radius: 18; " +
            "-fx-padding: 12 16;"
        );
        
        Label messageLabel = new Label(message);
        messageLabel.setWrapText(true);
        messageLabel.setMaxWidth(400);
        messageLabel.setStyle("-fx-text-fill: white; -fx-font-size: 14px;");
        
        bubble.getChildren().add(messageLabel);
        messageBox.getChildren().add(bubble);
        
        chatContainer.getChildren().add(messageBox);
    }

    private void addAssistantMessage(String message) {
        HBox messageBox = new HBox(10);
        messageBox.setAlignment(Pos.CENTER_LEFT);
        messageBox.setPadding(new Insets(5, 50, 5, 10));
        
        Label iconLabel = new Label("🤖");
        iconLabel.setStyle("-fx-font-size: 24px;");
        
        VBox bubble = new VBox(5);
        bubble.setStyle(
            "-fx-background-color: #F0F0F0; " +
            "-fx-background-radius: 18; " +
            "-fx-padding: 12 16;"
        );
        
        Label messageLabel = new Label(message);
        messageLabel.setWrapText(true);
        messageLabel.setMaxWidth(400);
        messageLabel.setStyle("-fx-text-fill: #1A1A1B; -fx-font-size: 14px;");
        
        bubble.getChildren().add(messageLabel);
        messageBox.getChildren().addAll(iconLabel, bubble);
        
        chatContainer.getChildren().add(messageBox);
    }

    @FXML
    private void clearChat() {
        chatContainer.getChildren().clear();
        conversationHistory.clear();
        addAssistantMessage("👋 Conversation réinitialisée. Comment puis-je vous aider ?");
    }
}
