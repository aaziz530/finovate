package org.esprit.finovate.controllers;

import org.esprit.finovate.entities.Ad;
import org.esprit.finovate.services.MarketplaceService;
import org.esprit.finovate.utils.ImageUtils;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import java.io.File;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;

public class AdsAdminController {

    @FXML private TextField txtTitre;
    @FXML private TextField txtImage;
    @FXML private TextField txtDuree;
    @FXML private TextField txtPoints;

    @FXML private TableView<Ad> tableAds;
    @FXML private TableColumn<Ad, String> colTitre;
    @FXML private TableColumn<Ad, Integer> colDuree;
    @FXML private TableColumn<Ad, Integer> colPoints;

    private MarketplaceService service;
    private ObservableList<Ad> adsList;

    @FXML private TextField txtRechercheAds;

    @FXML
    public void initialize() {
        service = new MarketplaceService();
        adsList = FXCollections.observableArrayList();

        colTitre.setCellValueFactory(new PropertyValueFactory<>("title"));
        colDuree.setCellValueFactory(new PropertyValueFactory<>("duration"));
        colPoints.setCellValueFactory(new PropertyValueFactory<>("rewardPoints"));

        loadAds();

        FilteredList<Ad> filteredData = new FilteredList<>(adsList, a -> true);

        if (txtRechercheAds != null) {
            txtRechercheAds.textProperty().addListener((observable, oldValue, newValue) -> {
                filteredData.setPredicate(ad -> {
                    if (newValue == null || newValue.isEmpty()) return true;
                    String lowerCaseFilter = newValue.toLowerCase();

                    if (ad.getTitle() != null && ad.getTitle().toLowerCase().contains(lowerCaseFilter)) return true;
                    if (String.valueOf(ad.getRewardPoints()).contains(lowerCaseFilter)) return true;
                    return false;
                });
            });
        }

        SortedList<Ad> sortedData = new SortedList<>(filteredData);
        sortedData.comparatorProperty().bind(tableAds.comparatorProperty());
        tableAds.setItems(sortedData);

        tableAds.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) remplirFormulaire(newSelection);
        });
    }

    private void loadAds() {
        adsList.clear();
        adsList.addAll(service.getAllAds());
    }

    private void remplirFormulaire(Ad ad) {
        txtTitre.setText(ad.getTitle());
        txtImage.setText(ad.getImagePath());
        txtDuree.setText(String.valueOf(ad.getDuration()));
        txtPoints.setText(String.valueOf(ad.getRewardPoints()));
    }

    private boolean validerSaisie() {
        StringBuilder errorMessage = new StringBuilder();

        if (txtTitre.getText() == null || txtTitre.getText().trim().isEmpty()) {
            errorMessage.append("- Le titre de la publicité est obligatoire.\n");
        }
        if (txtImage.getText() == null || txtImage.getText().trim().isEmpty()) {
            errorMessage.append("- Veuillez sélectionner une image.\n");
        }

        if (txtDuree.getText() == null || txtDuree.getText().trim().isEmpty()) {
            errorMessage.append("- La durée est obligatoire.\n");
        } else {
            try {
                int duree = Integer.parseInt(txtDuree.getText().trim());
                if (duree <= 0) {
                    errorMessage.append("- La durée doit être strictement positive (> 0 seconde).\n");
                }
            } catch (NumberFormatException e) {
                errorMessage.append("- La durée doit être un nombre entier valide.\n");
            }
        }

        if (txtPoints.getText() == null || txtPoints.getText().trim().isEmpty()) {
            errorMessage.append("- Le nombre de points est obligatoire.\n");
        } else {
            try {
                int points = Integer.parseInt(txtPoints.getText().trim());
                if (points < 0) {
                    errorMessage.append("- Les points gagnés ne peuvent pas être négatifs.\n");
                }
            } catch (NumberFormatException e) {
                errorMessage.append("- Les points doivent être un nombre entier valide.\n");
            }
        }

        if (errorMessage.length() == 0) {
            return true;
        } else {
            showAlert("Erreur de saisie", errorMessage.toString(), Alert.AlertType.ERROR);
            return false;
        }
    }

    @FXML
    public void ajouter() {
        if (validerSaisie()) {
            Ad ad = new Ad(0, txtTitre.getText().trim(), txtImage.getText().trim(),
                    Integer.parseInt(txtDuree.getText().trim()),
                    Integer.parseInt(txtPoints.getText().trim()));
            service.addAd(ad);
            clearForm();
            loadAds();
            showAlert("Succès", "Publicité ajoutée avec succès.", Alert.AlertType.INFORMATION);
        }
    }

    @FXML
    public void modifier() {
        Ad selected = tableAds.getSelectionModel().getSelectedItem();
        if (selected != null) {
            if (validerSaisie()) {
                // Keep existing image if no new image is selected
                String imagePath = txtImage.getText().trim();
                if (imagePath.isEmpty()) {
                    imagePath = selected.getImagePath();
                }
                Ad ad = new Ad(selected.getId(), txtTitre.getText().trim(), imagePath,
                        Integer.parseInt(txtDuree.getText().trim()),
                        Integer.parseInt(txtPoints.getText().trim()));
                service.updateAd(ad);
                clearForm();
                loadAds();
                showAlert("Succès", "Publicité modifiée avec succès.", Alert.AlertType.INFORMATION);
            }
        } else {
            showAlert("Attention", "Sélectionnez une publicité à modifier dans le tableau.", Alert.AlertType.WARNING);
        }
    }

    @FXML
    public void supprimer() {
        Ad selected = tableAds.getSelectionModel().getSelectedItem();
        if (selected != null) {
            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, "Voulez-vous vraiment supprimer cette publicité ?", ButtonType.YES, ButtonType.NO);
            confirm.showAndWait().ifPresent(response -> {
                if (response == ButtonType.YES) {
                    service.deleteAd(selected.getId());
                    clearForm();
                    loadAds();
                    showAlert("Succès", "Publicité supprimée.", Alert.AlertType.INFORMATION);
                }
            });
        } else {
            showAlert("Attention", "Sélectionnez une publicité à supprimer dans le tableau.", Alert.AlertType.WARNING);
        }
    }

    private void clearForm() {
        txtTitre.clear();
        txtImage.clear();
        txtDuree.clear();
        txtPoints.clear();
    }

    private void showAlert(String title, String content, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    @FXML
    public void choisirImage() {
        File selectedFile = ImageUtils.selectImageFile(txtImage.getScene().getWindow());
        if (selectedFile == null) {
            return;
        }

        try {
            String savedPath = ImageUtils.saveImage(selectedFile);
            txtImage.setText(savedPath);
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Erreur technique", "Impossible de sauvegarder l'image : " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }
}
