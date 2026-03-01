package org.esprit.finovate.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import org.esprit.finovate.services.PersonalityService;

public class PersonalityViewController {

    @FXML private Label emojiLabel;
    @FXML private Label typeLabel;
    @FXML private Label usernameLabel;
    @FXML private Label titleLabel;
    @FXML private Label descriptionLabel;
    @FXML private Label positiveLabel;
    @FXML private Label neutralLabel;
    @FXML private Label negativeLabel;
    @FXML private VBox traitsSection;
    @FXML private VBox traitsContainer;

    private MainController mainController;

    public void setMainController(MainController mainController) {
        this.mainController = mainController;
    }

    public void displayAnalysis(PersonalityService.PersonalityAnalysis analysis, String username) {
        emojiLabel.setText(analysis.emoji);
        typeLabel.setText(analysis.personalityType);
        usernameLabel.setText("👤 " + username);
        titleLabel.setText("🏆 " + analysis.title);
        descriptionLabel.setText(analysis.description);
        
        positiveLabel.setText(String.format("😊 Positif: %.0f%%", analysis.positiveScore * 100));
        neutralLabel.setText(String.format("😐 Neutre: %.0f%%", analysis.neutralScore * 100));
        negativeLabel.setText(String.format("😔 Négatif: %.0f%%", analysis.negativeScore * 100));
        
        // Add traits
        traitsContainer.getChildren().clear();
        if (analysis.traits != null && !analysis.traits.isEmpty()) {
            for (String trait : analysis.traits) {
                Label traitLabel = new Label("• " + trait);
                traitLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #333;");
                traitLabel.setWrapText(true);
                traitsContainer.getChildren().add(traitLabel);
            }
            traitsSection.setVisible(true);
            traitsSection.setManaged(true);
        } else {
            traitsSection.setVisible(false);
            traitsSection.setManaged(false);
        }
    }
}
