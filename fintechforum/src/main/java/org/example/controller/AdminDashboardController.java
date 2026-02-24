package org.example.controller;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.example.entities.User;
import org.example.services.UserService;
import org.example.utils.SessionManager;

import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.ResourceBundle;

public class AdminDashboardController implements Initializable {

    @FXML
    private Label adminNameLabel;

    @FXML
    private Button logoutButton;

    @FXML
    private Label totalUsersLabel;

    @FXML
    private Label totalReclamationsLabel;

    @FXML
    private TextField searchField;

    @FXML
    private TableView<User> usersTable;

    @FXML
    private TableColumn<User, Long> idColumn;

    @FXML
    private TableColumn<User, String> firstNameColumn;

    @FXML
    private TableColumn<User, String> lastNameColumn;

    @FXML
    private TableColumn<User, String> emailColumn;

    @FXML
    private TableColumn<User, String> roleColumn;

    @FXML
    private TableColumn<User, Integer> pointsColumn;

    @FXML
    private TableColumn<User, Float> soldeColumn;

    @FXML
    private TableColumn<User, String> createdAtColumn;

    @FXML
    private TableColumn<User, String> cinColumn;

    @FXML
    private TableColumn<User, Long> numeroCarteColumn;

    @FXML
    private TableColumn<User, Void> actionsColumn;

    // Update Dialog fields
    @FXML
    private TextField updateFirstNameField;

    @FXML
    private TextField updateLastNameField;

    @FXML
    private TextField updateEmailField;

    @FXML
    private DatePicker updateBirthdatePicker;

    @FXML
    private ComboBox<String> updateRoleComboBox;

    @FXML
    private TextField updatePointsField;

    @FXML
    private TextField updateSoldeField;

    @FXML
    private TextField updateCinField;

    @FXML
    private TextField updateNumeroCarteField;

    @FXML
    private Label updateErrorLabel;

    @FXML
    private Button updateCancelButton;

    @FXML
    private Button updateSaveButton;

    private final UserService userService;
    private final ObservableList<User> usersList = FXCollections.observableArrayList();
    private User selectedUserForUpdate;

    public AdminDashboardController() {
        this.userService = new UserService();
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupAdminInfo();
        setupStatistics();
        setupUserTable();
        loadUsers();
    }

    private void setupAdminInfo() {
        if (adminNameLabel != null && SessionManager.getCurrentUser() != null) {
            String name = SessionManager.getCurrentUser().getFirstName() != null
                 ? SessionManager.getCurrentUser().getFirstName()
                 : SessionManager.getCurrentUser().getEmail();
            adminNameLabel.setText("Welcome, " + name);
        }
    }

    private void setupStatistics() {
        try {
            List<User> allUsers = userService.getAllUsers();
            if (totalUsersLabel != null) {
                totalUsersLabel.setText(String.valueOf(allUsers.size()));
            }

            if (totalReclamationsLabel != null) {
                totalReclamationsLabel.setText("0");
            }

        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Error loading statistics: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void setupUserTable() {
        if (usersTable == null)
            return;

        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        firstNameColumn.setCellValueFactory(new PropertyValueFactory<>("firstName"));
        lastNameColumn.setCellValueFactory(new PropertyValueFactory<>("lastName"));
        emailColumn.setCellValueFactory(new PropertyValueFactory<>("email"));
        roleColumn.setCellValueFactory(cellData ->
             new SimpleStringProperty(cellData.getValue().getRole()));
        pointsColumn.setCellValueFactory(new PropertyValueFactory<>("points"));
        soldeColumn.setCellValueFactory(new PropertyValueFactory<>("solde"));
        cinColumn.setCellValueFactory(new PropertyValueFactory<>("cinNumber"));
        numeroCarteColumn.setCellValueFactory(new PropertyValueFactory<>("numeroCarte"));

        createdAtColumn.setCellValueFactory(cellData -> {
            if (cellData.getValue().getCreatedAt() != null) {
                SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm");
                return new SimpleStringProperty(sdf.format(cellData.getValue().getCreatedAt()));
            }
            return new SimpleStringProperty("");
        });

        setupActionsColumn();
        usersTable.setItems(usersList);
    }

    private void setupActionsColumn() {
        actionsColumn.setCellFactory(param -> new TableCell<>() {
            private final Button editButton = new Button("Edit");
            private final Button blockButton = new Button("Block");
            private final Button unblockButton = new Button("Unblock");
            private final Button deleteButton = new Button("Delete");
            private final HBox container = new HBox(8, editButton, blockButton, unblockButton, deleteButton);

            {
                editButton.setStyle("-fx-background-color: #2c5aa0; -fx-text-fill: white; -fx-cursor: hand; -fx-font-size: 11px;");
                blockButton.setStyle("-fx-background-color: #ffc107; -fx-text-fill: white; -fx-cursor: hand; -fx-font-size: 11px;");
                unblockButton.setStyle("-fx-background-color: #28a745; -fx-text-fill: white; -fx-cursor: hand; -fx-font-size: 11px;");
                deleteButton.setStyle("-fx-background-color: #dc3545; -fx-text-fill: white; -fx-cursor: hand; -fx-font-size: 11px;");
                container.setAlignment(Pos.CENTER);

                editButton.setOnAction(event -> {
                    User user = getTableView().getItems().get(getIndex());
                    handleEditUser(user);
                });

                blockButton.setOnAction(event -> {
                    User user = getTableView().getItems().get(getIndex());
                    handleBlockUser(user);
                });

                unblockButton.setOnAction(event -> {
                    User user = getTableView().getItems().get(getIndex());
                    handleUnblockUser(user);
                });

                deleteButton.setOnAction(event -> {
                    User user = getTableView().getItems().get(getIndex());
                    handleDeleteUser(user);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                    return;
                }

                User user = getTableView().getItems().get(getIndex());
                if (user != null && "ADMIN".equals(user.getRole())) {
                    setGraphic(null);
                } else {
                    blockButton.setVisible(!user.isBlocked());
                    blockButton.setManaged(!user.isBlocked());
                    unblockButton.setVisible(user.isBlocked());
                    unblockButton.setManaged(user.isBlocked());
                    setGraphic(container);
                }
            }
        });
    }

    private void loadUsers() {
        try {
            List<User> users = userService.getAllUsers();
            usersList.clear();
            usersList.addAll(users);
            setupStatistics();
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Error loading users: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void handleSearch() {
        String searchTerm = searchField.getText().trim().toLowerCase();

        if (searchTerm.isEmpty()) {
            loadUsers();
            return;
        }

        try {
            List<User> allUsers = userService.getAllUsers();
            usersList.clear();
            usersList.addAll(allUsers.stream()
                .filter(user ->
                     (user.getFirstName() != null && user.getFirstName().toLowerCase().contains(searchTerm)) ||
                    (user.getLastName() != null && user.getLastName().toLowerCase().contains(searchTerm)) ||
                    user.getEmail().toLowerCase().contains(searchTerm))
                .toList());
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Search error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void handleEditUser(User user) {
        try {
            selectedUserForUpdate = user;
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/user-update-dialog.fxml"));
            loader.setController(this);
            Parent root = loader.load();

            populateUpdateDialog(user);

            Stage dialogStage = new Stage();
            dialogStage.setTitle("Update User");
            dialogStage.initModality(Modality.APPLICATION_MODAL);
            dialogStage.setScene(new Scene(root));
            dialogStage.setResizable(false);
            dialogStage.showAndWait();

        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Failed to open update dialog: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void populateUpdateDialog(User user) {
        if (updateFirstNameField != null && user.getFirstName() != null)
            updateFirstNameField.setText(user.getFirstName());
        if (updateLastNameField != null && user.getLastName() != null)
            updateLastNameField.setText(user.getLastName());
        if (updateEmailField != null)
            updateEmailField.setText(user.getEmail());
        if (updatePointsField != null)
            updatePointsField.setText(String.valueOf(user.getPoints()));
        if (updateSoldeField != null)
            updateSoldeField.setText(String.valueOf(user.getSolde()));
        if (updateCinField != null && user.getCinNumber() != null)
            updateCinField.setText(user.getCinNumber());
        if (updateNumeroCarteField != null && user.getNumeroCarte() != null)
            updateNumeroCarteField.setText(String.valueOf(user.getNumeroCarte()));

        if (updateBirthdatePicker != null && user.getBirthdate() != null) {
            updateBirthdatePicker.setValue(new java.util.Date(user.getBirthdate().getTime())
                    .toInstant()
                    .atZone(java.time.ZoneId.systemDefault()).toLocalDate());
        }

        if (updateRoleComboBox != null) {
            updateRoleComboBox.setItems(FXCollections.observableArrayList("USER", "MODERATOR", "ADMIN"));
            updateRoleComboBox.setValue(user.getRole());
        }
    }

    @FXML
    private void handleUpdateSave() {
        if (selectedUserForUpdate == null)
            return;

        try {
            if (updateFirstNameField.getText().trim().isEmpty() ||
                    updateLastNameField.getText().trim().isEmpty() ||
                    updateEmailField.getText().trim().isEmpty() ||
                    updateCinField.getText().trim().isEmpty()) {
                showUpdateError("All required fields must be filled");
                return;
            }

            String cinNumber = updateCinField.getText().trim();
            if (!cinNumber.matches("\\d{8}")) {
                showUpdateError("CIN Number must be exactly 8 digits");
                return;
            }

            selectedUserForUpdate.setFirstName(updateFirstNameField.getText().trim());
            selectedUserForUpdate.setLastName(updateLastNameField.getText().trim());
            selectedUserForUpdate.setEmail(updateEmailField.getText().trim());
            selectedUserForUpdate.setRole(updateRoleComboBox.getValue());
            selectedUserForUpdate.setCinNumber(updateCinField.getText().trim());

            try {
                selectedUserForUpdate.setPoints(Integer.parseInt(updatePointsField.getText().trim()));
                selectedUserForUpdate.setSolde(Float.parseFloat(updateSoldeField.getText().trim()));
                             
                if (!updateNumeroCarteField.getText().trim().isEmpty()) {
                    selectedUserForUpdate.setNumeroCarte(Long.parseLong(updateNumeroCarteField.getText().trim()));
                }
            } catch (NumberFormatException e) {
                showUpdateError("Points, Balance, and Card Number must be valid numbers");
                return;
            }

            if (updateBirthdatePicker.getValue() != null) {
                selectedUserForUpdate.setBirthdate(java.util.Date.from(updateBirthdatePicker.getValue()
                        .atStartOfDay(java.time.ZoneId.systemDefault()).toInstant()));
            }

            userService.updateUser(selectedUserForUpdate);

            loadUsers();
            closeUpdateDialog();
            showAlert(Alert.AlertType.INFORMATION, "User updated successfully");

        } catch (SQLException e) {
            showUpdateError("Database error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void handleUpdateCancel() {
        closeUpdateDialog();
    }

    private void closeUpdateDialog() {
        if (updateCancelButton != null) {
            Stage stage = (Stage) updateCancelButton.getScene().getWindow();
            stage.close();
        }
    }

    private void showUpdateError(String message) {
        if (updateErrorLabel != null) {
            updateErrorLabel.setText(message);
            updateErrorLabel.setVisible(true);
        }
    }

    private void handleBlockUser(User user) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Block User");
        confirm.setHeaderText(null);
        confirm.setContentText("Are you sure you want to block " + user.getEmail() + "?");
             
        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    userService.blockUser(user.getId());
                    loadUsers();
                    showAlert(Alert.AlertType.INFORMATION, "User blocked successfully");
                } catch (SQLException e) {
                    showAlert(Alert.AlertType.ERROR, "Failed to block user: " + e.getMessage());
                }
            }
        });
    }

    private void handleUnblockUser(User user) {
        try {
            userService.unblockUser(user.getId());
            loadUsers();
            showAlert(Alert.AlertType.INFORMATION, "User unblocked successfully");
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Failed to unblock user: " + e.getMessage());
        }
    }

    private void handleDeleteUser(User user) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Delete User");
        confirm.setHeaderText(null);
        confirm.setContentText("Are you sure you want to delete " + user.getEmail() + "? This action cannot be undone.");
             
        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    userService.deleteUser(user.getId());
                    loadUsers();
                    showAlert(Alert.AlertType.INFORMATION, "User deleted successfully");
                } catch (SQLException e) {
                    showAlert(Alert.AlertType.ERROR, "Failed to delete user: " + e.getMessage());
                }
            }
        });
    }

    @FXML
    private void handleLogout() {
        SessionManager.logout();

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/login.fxml"));
            Parent root = loader.load();

            Stage stage = (Stage) logoutButton.getScene().getWindow();
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.setTitle("Login - Finovate");
            stage.centerOnScreen();

        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Failed to logout: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void showAlert(Alert.AlertType type, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(type == Alert.AlertType.ERROR ? "Error" : "Information");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
