package org.esprit.finovate.controllers;

import org.esprit.finovate.entities.Product;
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

public class ProductsAdminController {

    @FXML private TextField txtNom;
    @FXML private TextField txtDescription;
    @FXML private TextField txtPrix;
    @FXML private TextField txtImage;
    @FXML private TextField txtStock;

    @FXML private TableView<Product> tableProducts;
    @FXML private TableColumn<Product, String> colNom;
    @FXML private TableColumn<Product, String> colDescription;
    @FXML private TableColumn<Product, Integer> colPrix;
    @FXML private TableColumn<Product, Integer> colStock;

    private MarketplaceService service;
    private ObservableList<Product> productsList;

    @FXML private TextField txtRechercheProducts;

    @FXML
    public void initialize() {
        service = new MarketplaceService();
        productsList = FXCollections.observableArrayList();

        colNom.setCellValueFactory(new PropertyValueFactory<>("name"));
        colDescription.setCellValueFactory(new PropertyValueFactory<>("description"));
        colPrix.setCellValueFactory(new PropertyValueFactory<>("pricePoints"));
        colStock.setCellValueFactory(new PropertyValueFactory<>("stock"));

        loadProducts();

        FilteredList<Product> filteredData = new FilteredList<>(productsList, p -> true);

        if (txtRechercheProducts != null) {
            txtRechercheProducts.textProperty().addListener((observable, oldValue, newValue) -> {
                filteredData.setPredicate(product -> {
                    if (newValue == null || newValue.isEmpty()) return true;
                    String lowerCaseFilter = newValue.toLowerCase();

                    if (product.getName() != null && product.getName().toLowerCase().contains(lowerCaseFilter)) return true;
                    if (product.getDescription() != null && product.getDescription().toLowerCase().contains(lowerCaseFilter)) return true;
                    return false;
                });
            });
        }

        SortedList<Product> sortedData = new SortedList<>(filteredData);
        sortedData.comparatorProperty().bind(tableProducts.comparatorProperty());
        tableProducts.setItems(sortedData);

        tableProducts.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) remplirFormulaire(newSelection);
        });
    }

    private void loadProducts() {
        productsList.clear();
        productsList.addAll(service.getAllProducts());
    }

    private void remplirFormulaire(Product product) {
        txtNom.setText(product.getName());
        txtDescription.setText(product.getDescription());
        txtPrix.setText(String.valueOf(product.getPricePoints()));
        txtImage.setText(product.getImage());
        txtStock.setText(String.valueOf(product.getStock()));
    }

    private boolean validerSaisie() {
        StringBuilder errorMessage = new StringBuilder();

        if (txtNom.getText() == null || txtNom.getText().trim().isEmpty()) {
            errorMessage.append("- Le nom du produit est obligatoire.\n");
        }
        if (txtDescription.getText() == null || txtDescription.getText().trim().isEmpty()) {
            errorMessage.append("- La description est obligatoire.\n");
        }

        if (txtPrix.getText() == null || txtPrix.getText().trim().isEmpty()) {
            errorMessage.append("- Le prix en points est obligatoire.\n");
        } else {
            try {
                int prix = Integer.parseInt(txtPrix.getText().trim());
                if (prix < 0) {
                    errorMessage.append("- Le prix ne peut pas être négatif.\n");
                }
            } catch (NumberFormatException e) {
                errorMessage.append("- Le prix doit être un nombre entier valide.\n");
            }
        }

        if (txtStock.getText() == null || txtStock.getText().trim().isEmpty()) {
            errorMessage.append("- Le stock est obligatoire.\n");
        } else {
            try {
                int stock = Integer.parseInt(txtStock.getText().trim());
                if (stock < 0) {
                    errorMessage.append("- Le stock ne peut pas être négatif.\n");
                }
            } catch (NumberFormatException e) {
                errorMessage.append("- Le stock doit être un nombre entier valide.\n");
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
            Product product = new Product(
                    0,
                    txtNom.getText().trim(),
                    txtDescription.getText().trim(),
                    Integer.parseInt(txtPrix.getText().trim()),
                    txtImage.getText().trim(),
                    Integer.parseInt(txtStock.getText().trim())
            );
            service.addProduct(product);
            clearForm();
            loadProducts();
            showAlert("Succès", "Produit ajouté avec succès.", Alert.AlertType.INFORMATION);
        }
    }

    @FXML
    public void modifier() {
        Product selected = tableProducts.getSelectionModel().getSelectedItem();
        if (selected != null) {
            if (validerSaisie()) {
                // Keep existing image if no new image is selected
                String imagePath = txtImage.getText().trim();
                if (imagePath.isEmpty()) {
                    imagePath = selected.getImage();
                }
                Product product = new Product(
                        selected.getId(),
                        txtNom.getText().trim(),
                        txtDescription.getText().trim(),
                        Integer.parseInt(txtPrix.getText().trim()),
                        imagePath,
                        Integer.parseInt(txtStock.getText().trim())
                );
                service.updateProduct(product);
                clearForm();
                loadProducts();
                showAlert("Succès", "Produit modifié avec succès.", Alert.AlertType.INFORMATION);
            }
        } else {
            showAlert("Attention", "Sélectionnez un produit à modifier dans le tableau.", Alert.AlertType.WARNING);
        }
    }

    @FXML
    public void supprimer() {
        Product selected = tableProducts.getSelectionModel().getSelectedItem();
        if (selected != null) {
            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, "Voulez-vous vraiment supprimer ce produit ?", ButtonType.YES, ButtonType.NO);
            confirm.showAndWait().ifPresent(response -> {
                if (response == ButtonType.YES) {
                    service.deleteProduct(selected.getId());
                    clearForm();
                    loadProducts();
                    showAlert("Succès", "Produit supprimé.", Alert.AlertType.INFORMATION);
                }
            });
        } else {
            showAlert("Attention", "Sélectionnez un produit à supprimer dans le tableau.", Alert.AlertType.WARNING);
        }
    }

    private void clearForm() {
        txtNom.clear();
        txtDescription.clear();
        txtPrix.clear();
        txtImage.clear();
        txtStock.clear();
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
