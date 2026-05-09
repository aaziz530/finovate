package org.esprit.finovate.controllers;

import org.esprit.finovate.entities.Ad;
import org.esprit.finovate.entities.Product;
import org.esprit.finovate.services.MarketplaceService;
import org.esprit.finovate.utils.ImageUtils;
import org.esprit.finovate.utils.Session;
import javafx.scene.Node;
import javafx.scene.control.ButtonBar;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.ButtonType;
import javafx.util.Pair;

import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import java.util.Comparator;
import java.util.stream.Collectors;

import javafx.scene.control.Button;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class MarketplaceController {

    @FXML private Label lblSoldePoints;
    @FXML private GridPane gridProduits;
    @FXML private ScrollPane scrollPane;

    private MarketplaceService service;

    @FXML private TextField txtRecherche;
    @FXML private ComboBox<String> comboTri;
    private List<Product> allProductsList = new ArrayList<>();

    @FXML
    public void initialize() {
        service = new MarketplaceService();

        // Initialisation du ComboBox de tri
        comboTri.getItems().addAll("Par défaut", "Prix : Croissant", "Prix : Décroissant", "Nom : A - Z", "Nom : Z - A");
        comboTri.setValue("Par défaut");

        // Écouteurs d'événements
        txtRecherche.textProperty().addListener((observable, oldValue, newValue) -> filtrerEtTrierProduits());
        comboTri.setOnAction(event -> filtrerEtTrierProduits());

        refreshData();
    }

    private void refreshData() {
        int points = service.getUserPoints(Session.currentUser.getId());
        lblSoldePoints.setText(points + " pts");

        allProductsList = service.getAllProducts();
        filtrerEtTrierProduits();
    }

    private void filtrerEtTrierProduits() {
        String recherche = txtRecherche.getText().toLowerCase().trim();
        String typeTri = comboTri.getValue();

        // 1. Filtrage
        List<Product> produitsFiltres = allProductsList.stream()
                .filter(p -> p.getName().toLowerCase().contains(recherche) ||
                        (p.getDescription() != null && p.getDescription().toLowerCase().contains(recherche)))
                .collect(Collectors.toList());

        // 2. Tri
        switch (typeTri) {
            case "Prix : Croissant":
                produitsFiltres.sort(Comparator.comparingInt(Product::getPricePoints));
                break;
            case "Prix : Décroissant":
                produitsFiltres.sort((p1, p2) -> Integer.compare(p2.getPricePoints(), p1.getPricePoints()));
                break;
            case "Nom : A - Z":
                produitsFiltres.sort(Comparator.comparing(Product::getName, String.CASE_INSENSITIVE_ORDER));
                break;
            case "Nom : Z - A":
                produitsFiltres.sort((p1, p2) -> p2.getName().compareToIgnoreCase(p1.getName()));
                break;
        }

        // 3. Affichage
        gridProduits.getChildren().clear();
        int column = 0;
        int row = 1;

        for (Product p : produitsFiltres) {
            VBox productCard = createProductCard(p);
            gridProduits.add(productCard, column, row);
            column++;
            if (column == 3) {
                column = 0;
                row++;
            }
        }
    }

    private VBox createProductCard(Product p) {
        VBox card = new VBox(10);
        card.setPadding(new Insets(15));
        card.setStyle("-fx-background-color: white; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 10, 0, 0, 0); -fx-background-radius: 10;");
        card.setAlignment(Pos.CENTER);

        ImageView imgView = new ImageView();
        imgView.setFitHeight(100);
        imgView.setFitWidth(100);

        try {
            String imagePath = p.getImage();
            if (imagePath == null || imagePath.isEmpty()) {
                imagePath = "/images/default_product.png";
            }

            Image image = ImageUtils.loadImage(imagePath);
            if (image == null) {
                image = ImageUtils.loadImage("/images/default_product.png");
            }
            if (image != null) {
                imgView.setImage(image);
            }

        } catch (Exception e) {
            System.out.println("Erreur chargement image : " + e.getMessage());
        }
        Label nameLabel = new Label(p.getName());
        nameLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");

        Label priceLabel = new Label(p.getPricePoints() + " Pts");
        priceLabel.setStyle("-fx-text-fill: #2ecc71; -fx-font-weight: bold;");

        Button buyButton = new Button("Acheter");
        buyButton.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-cursor: hand;");

        buyButton.setOnAction(event -> handlePurchase(p));

        card.getChildren().addAll(imgView, nameLabel, priceLabel, buyButton);
        return card;
    }

    private void handlePurchase(Product p) {
        boolean success = service.buyProduct(Session.currentUser.getId(), p);
        if (success) {
            showAlert(Alert.AlertType.INFORMATION, "Succès", "Vous avez acheté : " + p.getName());
            refreshData();
        } else {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Solde insuffisant !");
        }
    }

    @FXML
    public void onWatchAd() {
        Ad ad = service.getSmartAdForUser(Session.currentUser.getId());
        if (ad == null) {
            showAlert(Alert.AlertType.WARNING, "Désolé", "Aucune publicité disponible.");
            return;
        }

        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Sponsor : " + ad.getTitle());
        dialog.setHeaderText("Regardez cette pub pour gagner des points");

        ButtonType loginButtonType = new ButtonType("Réclamer mes points", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(loginButtonType);

        Node btnReclamer = dialog.getDialogPane().lookupButton(loginButtonType);
        btnReclamer.setDisable(true);

        VBox content = new VBox(10);
        content.setAlignment(Pos.CENTER);

        ImageView adImage = new ImageView();
        try {
            String path = ad.getImagePath();
            if (path == null || path.isEmpty()) {
                path = "/images/default_ad.png";
            }

            Image image = ImageUtils.loadImage(path);
            if (image == null) {
                image = ImageUtils.loadImage("/images/default_ad.png");
            }
            if (image != null) {
                adImage.setImage(image);
            }
            adImage.setFitWidth(300);
            adImage.setFitHeight(150);
            adImage.setPreserveRatio(true);
        } catch (Exception e) { /* Ignorer */ }

        Label lblTimer = new Label("Chargement...");
        lblTimer.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #e74c3c;");

        content.getChildren().addAll(adImage, lblTimer);
        dialog.getDialogPane().setContent(content);

        Task<Void> waitTask = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                for (int i = ad.getDuration(); i > 0; i--) {
                    updateMessage("Patience... " + i + " s");
                    Thread.sleep(1000);
                }
                updateMessage("Terminé ! Cliquez sur le bouton.");
                return null;
            }
        };

        lblTimer.textProperty().bind(waitTask.messageProperty());

        waitTask.setOnSucceeded(e -> {
            btnReclamer.setDisable(false);
            btnReclamer.setStyle("-fx-base: #2ecc71;");
            btnReclamer.requestFocus();
        });

        new Thread(waitTask).start();

        dialog.showAndWait().ifPresent(response -> {
            if (response == loginButtonType) {
                service.watchAd(Session.currentUser.getId(), ad);
                showAlert(Alert.AlertType.INFORMATION, "Bravo !", "Vous avez gagné " + ad.getRewardPoints() + " points !");
                refreshData();
            }
        });
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
