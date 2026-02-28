package org.esprit.finovate.controllers;

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
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.esprit.finovate.entities.User;
import org.esprit.finovate.services.IUserService;
import org.esprit.finovate.services.PDFExportService;
import org.esprit.finovate.services.UserService;
import org.esprit.finovate.utils.Session;

import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.Period;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.ResourceBundle;

/**
 * Controller for Admin Dashboard operations
 */
public class AdminDashboardController implements Initializable {

    // Top navigation
    @FXML
    private Label adminNameLabel;

    @FXML
    private Button logoutButton;

    @FXML
    private Button chatbotButton;

    // Statistics cards
    @FXML
    private Label totalUsersLabel;

    @FXML
    private Label totalReclamationsLabel;

    // User management
    @FXML
    private TextField searchField;

    @FXML
    private Button addUserButton;

    @FXML
    private TableView<User> usersTable;


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
    private TextField updateCinNumberField;

    @FXML
    private TextField updateNumeroCarteField;

    @FXML
    private Label updateErrorLabel;

    @FXML
    private Button updateCancelButton;

    @FXML
    private Button updateSaveButton;

    // Delete Dialog fields
    @FXML
    private Label deleteMessageLabel;

    @FXML
    private Label deleteUserInfoLabel;

    @FXML
    private Button deleteCancelButton;

    @FXML
    private Button deleteConfirmButton;

    @FXML
    private HBox filterPanel;

    @FXML
    private ComboBox<String> roleFilterCombo;

    @FXML
    private TextField minBalanceField;

    @FXML
    private TextField maxBalanceField;

    @FXML
    private DatePicker dateFilterPicker;

    @FXML
    private Button filterToggleButton;

    @FXML
    private Button exportPdfButton;

    @FXML
    private VBox mainContentArea;

    private ObservableList<javafx.scene.Node> savedAdminContent;
    private boolean chatbotVisible = false;

    // Service and data
    private final IUserService userService;
    private final ObservableList<User> usersList = FXCollections.observableArrayList();
    private User selectedUserForUpdate;
    private User selectedUserForDelete;

    public AdminDashboardController() {
        this.userService = new UserService();
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupAdminInfo();
        setupStatistics();
        setupUserTable();
        setupFilterControls();
        loadUsers();
    }

    @FXML
    private void handleChatbotTop() {
        if (mainContentArea == null) {
            showAlert(Alert.AlertType.ERROR, "Chat view container is missing (mainContentArea).");
            return;
        }

        if (!chatbotVisible) {
            // Save current content before switching
            savedAdminContent = FXCollections.observableArrayList(mainContentArea.getChildren());
            
            try {
                Parent chatbotView = FXMLLoader.load(getClass().getResource("/ChatbotView.fxml"));
                mainContentArea.getChildren().setAll(chatbotView);
                VBox.setVgrow(chatbotView, Priority.ALWAYS);
                chatbotVisible = true;

                if (chatbotButton != null) {
                    chatbotButton.setText("Back");
                }
            } catch (IOException e) {
                showAlert(Alert.AlertType.ERROR, "Failed to load chatbot: " + e.getMessage());
                e.printStackTrace();
            }
        } else {
            // Restore saved content
            if (savedAdminContent != null) {
                mainContentArea.getChildren().setAll(savedAdminContent);
            }
            chatbotVisible = false;
            if (chatbotButton != null) {
                chatbotButton.setText("Finovate AI");
            }
        }
    }

    private void setupFilterControls() {
        if (roleFilterCombo != null) {
            roleFilterCombo.setItems(FXCollections.observableArrayList("ALL", "USER", "ADMIN"));
            roleFilterCombo.setValue("ALL");
            roleFilterCombo.setOnAction(e -> applyFilters());
        }

        if (dateFilterPicker != null) {
            dateFilterPicker.setOnAction(e -> applyFilters());
        }

        if (minBalanceField != null) {
            minBalanceField.textProperty().addListener((obs, oldVal, newVal) -> applyFilters());
        }

        if (maxBalanceField != null) {
            maxBalanceField.textProperty().addListener((obs, oldVal, newVal) -> applyFilters());
        }
    }

    @FXML
    private void toggleFilters() {
        if (filterPanel != null) {
            boolean isVisible = !filterPanel.isVisible();
            filterPanel.setVisible(isVisible);
            filterPanel.setManaged(isVisible);
            filterToggleButton.setText(isVisible ? "Hide Filters" : "Advanced Filters");
        }
    }

    @FXML
    private void clearFilters() {
        if (roleFilterCombo != null) roleFilterCombo.setValue("ALL");
        if (minBalanceField != null) minBalanceField.clear();
        if (maxBalanceField != null) maxBalanceField.clear();
        if (dateFilterPicker != null) dateFilterPicker.setValue(null);
        if (searchField != null) searchField.clear();
        loadUsers();
    }

    private void applyFilters() {
        String searchTerm = searchField.getText().toLowerCase().trim();
        String roleFilter = roleFilterCombo.getValue();
        String minBalStr = minBalanceField.getText().trim();
        String maxBalStr = maxBalanceField.getText().trim();
        LocalDate dateLimit = dateFilterPicker.getValue();

        try {
            List<User> allUsers = userService.getAllUsers();
            List<User> filteredUsers = allUsers.stream().filter(user -> {
                // Search term check
                boolean matchesSearch = searchTerm.isEmpty() ||
                        user.getFirstName().toLowerCase().contains(searchTerm) ||
                        user.getLastName().toLowerCase().contains(searchTerm) ||
                        user.getEmail().toLowerCase().contains(searchTerm) ||
                        user.getCinNumber().toLowerCase().contains(searchTerm);

                // Role check
                boolean matchesRole = "ALL".equals(roleFilter) || roleFilter.equalsIgnoreCase(user.getRole());

                // Balance check
                boolean matchesBalance = true;
                if (!minBalStr.isEmpty()) {
                    try {
                        matchesBalance = user.getSolde() >= Float.parseFloat(minBalStr);
                    } catch (NumberFormatException e) { /* ignore invalid input */ }
                }
                if (matchesBalance && !maxBalStr.isEmpty()) {
                    try {
                        matchesBalance = user.getSolde() <= Float.parseFloat(maxBalStr);
                    } catch (NumberFormatException e) { /* ignore invalid input */ }
                }

                // Date check
                boolean matchesDate = true;
                if (dateLimit != null && user.getCreatedAt() != null) {
                    LocalDate createdDate = user.getCreatedAt().toInstant()
                            .atZone(ZoneId.systemDefault()).toLocalDate();
                    matchesDate = createdDate.isAfter(dateLimit) || createdDate.isEqual(dateLimit);
                }

                return matchesSearch && matchesRole && matchesBalance && matchesDate;
            }).toList();

            usersList.setAll(filteredUsers);
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Filter error: " + e.getMessage());
        }
    }

    /**
     * Setup admin information display
     */
    private void setupAdminInfo() {
        if (adminNameLabel != null && Session.currentUser != null) {
            adminNameLabel.setText("Welcome, " + Session.currentUser.getFirstName());
        }
    }

    /**
     * Setup statistics cards
     */
    private void setupStatistics() {
        try {
            int totalUsers = userService.getTotalUsersCount();
            if (totalUsersLabel != null) {
                totalUsersLabel.setText(String.valueOf(totalUsers));
            }

            // Reclamations is static as requested
            if (totalReclamationsLabel != null) {
                totalReclamationsLabel.setText("0");
            }

        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Error loading statistics: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Setup user table columns and formatting
     */
    private void setupUserTable() {
        if (usersTable == null)
            return;

        // Configure columns
        firstNameColumn.setCellValueFactory(new PropertyValueFactory<>("firstName"));
        lastNameColumn.setCellValueFactory(new PropertyValueFactory<>("lastName"));
        emailColumn.setCellValueFactory(new PropertyValueFactory<>("email"));
        roleColumn.setCellValueFactory(new PropertyValueFactory<>("role"));
        pointsColumn.setCellValueFactory(new PropertyValueFactory<>("points"));
        soldeColumn.setCellValueFactory(new PropertyValueFactory<>("solde"));
        cinColumn.setCellValueFactory(new PropertyValueFactory<>("cinNumber"));
        numeroCarteColumn.setCellValueFactory(new PropertyValueFactory<>("numeroCarte"));

        // Format createdAt column
        createdAtColumn.setCellValueFactory(cellData -> {
            Date date = cellData.getValue().getCreatedAt();
            if (date != null) {
                SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm");
                return new SimpleStringProperty(sdf.format(date));
            }
            return new SimpleStringProperty("");
        });

        // Add action buttons column
        setupActionsColumn();

        // Set table data
        usersTable.setItems(usersList);
    }

    /**
     * Setup actions column with Edit and Delete buttons
     */
    private void setupActionsColumn() {
        actionsColumn.setCellFactory(param -> new TableCell<>() {
            private final Button editButton = new Button("Edit");
            private final Button deleteButton = new Button("Delete");
            private final HBox container = new HBox(10, editButton, deleteButton);

            {
                // Style buttons
                editButton.setStyle(
                        "-fx-background-color: #2c5aa0; -fx-text-fill: white; -fx-cursor: hand; -fx-font-size: 11px;");
                deleteButton.setStyle(
                        "-fx-background-color: #dc3545; -fx-text-fill: white; -fx-cursor: hand; -fx-font-size: 11px;");
                container.setAlignment(Pos.CENTER);

                // Add event handlers
                editButton.setOnAction(event -> {
                    User user = getTableView().getItems().get(getIndex());
                    handleEditUser(user);
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
                if (user != null && "ADMIN".equalsIgnoreCase(user.getRole())) {
                    setGraphic(null);
                } else {
                    setGraphic(container);
                }
            }
        });
    }

    /**
     * Load all users from database
     */
    private void loadUsers() {
        try {
            List<User> users = userService.getAllUsers();
            usersList.clear();
            // Filter out ADMIN users
            usersList.addAll(users.stream()
                    .toList());
            setupStatistics(); // Refresh statistics
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Error loading users: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Handle search functionality
     */
    @FXML
    private void handleSearch() {
        applyFilters();
    }

    /**
     * Handle edit user action
     */
    private void handleEditUser(User user) {
        try {
            selectedUserForUpdate = user;
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/UserUpdateDialog.fxml"));
            loader.setController(this);
            Parent root = loader.load();

            // Populate fields
            populateUpdateDialog(user);

            // Show dialog
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

    /**
     * Populate update dialog with user data
     */
    private void populateUpdateDialog(User user) {
        if (updateFirstNameField != null)
            updateFirstNameField.setText(user.getFirstName());
        if (updateLastNameField != null)
            updateLastNameField.setText(user.getLastName());
        if (updateEmailField != null)
            updateEmailField.setText(user.getEmail());
        if (updatePointsField != null)
            updatePointsField.setText(String.valueOf(user.getPoints()));
        if (updateSoldeField != null)
            updateSoldeField.setText(String.valueOf(user.getSolde()));
        if (updateCinNumberField != null)
            updateCinNumberField.setText(user.getCinNumber());
        if (updateNumeroCarteField != null)
            updateNumeroCarteField.setText(user.getNumeroCarte() != null ? String.valueOf(user.getNumeroCarte()) : "");

        if (updateBirthdatePicker != null && user.getBirthdate() != null) {
            updateBirthdatePicker.setValue(new Date(user.getBirthdate().getTime())
                    .toInstant()
                    .atZone(ZoneId.systemDefault()).toLocalDate());
        }

        if (updateRoleComboBox != null) {
            updateRoleComboBox.setItems(FXCollections.observableArrayList("USER", "ADMIN"));
            updateRoleComboBox.setValue(user.getRole());
        }
    }

    /**
     * Handle update save action
     */
    @FXML
    private void handleUpdateSave() {
        if (selectedUserForUpdate == null)
            return;

        try {
            if (updateFirstNameField == null || updateLastNameField == null || updateEmailField == null ||
                    updateCinNumberField == null || updatePointsField == null || updateSoldeField == null ||
                    updateRoleComboBox == null) {
                showUpdateError("Update dialog is not properly initialized (missing fields). Please reopen the dialog.");
                return;
            }

            // Validation
            if (updateFirstNameField.getText().trim().isEmpty() ||
                    updateLastNameField.getText().trim().isEmpty() ||
                    updateEmailField.getText().trim().isEmpty() ||
                    updateCinNumberField.getText().trim().isEmpty()) {
                showUpdateError("All required fields must be filled");
                return;
            }

            String firstName = updateFirstNameField.getText().trim();
            String lastName = updateLastNameField.getText().trim();
            String email = updateEmailField.getText().trim();

            if (firstName.length() < 3 || lastName.length() < 3) {
                showUpdateError("First name and last name must be at least 3 characters");
                return;
            }

            if (!firstName.matches("[A-Za-zÀ-ÖØ-öø-ÿ]+([ '\\-][A-Za-zÀ-ÖØ-öø-ÿ]+)*") ||
                    !lastName.matches("[A-Za-zÀ-ÖØ-öø-ÿ]+([ '\\-][A-Za-zÀ-ÖØ-öø-ÿ]+)*")) {
                showUpdateError("First name and last name must contain only letters");
                return;
            }

            if (!email.matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
                showUpdateError("Invalid email format");
                return;
            }

            String cinNumber = updateCinNumberField.getText().trim();
            if (!cinNumber.matches("\\d{8}")) {
                showUpdateError("CIN Number must be exactly 8 digits");
                return;
            }

            String cardNumberRaw = updateNumeroCarteField.getText() == null ? "" : updateNumeroCarteField.getText().trim();
            if (cardNumberRaw.isEmpty()) {
                showUpdateError("Card number is required and must be exactly 16 digits");
                return;
            }

            if (!cardNumberRaw.matches("\\d{16}")) {
                showUpdateError("Card number must be exactly 16 digits");
                return;
            }

            Long cardNumber;
            try {
                cardNumber = Long.parseLong(cardNumberRaw);
            } catch (NumberFormatException e) {
                showUpdateError("Card number must be a valid 16-digit number");
                return;
            }

            // Update user object
            selectedUserForUpdate.setFirstName(firstName);
            selectedUserForUpdate.setLastName(lastName);
            selectedUserForUpdate.setEmail(email);
            selectedUserForUpdate.setRole(updateRoleComboBox.getValue());
            selectedUserForUpdate.setCinNumber(updateCinNumberField.getText().trim());
            selectedUserForUpdate.setNumeroCarte(cardNumber);

            try {
                selectedUserForUpdate.setPoints(Integer.parseInt(updatePointsField.getText().trim()));
                selectedUserForUpdate.setSolde(Float.parseFloat(updateSoldeField.getText().trim()));
            } catch (NumberFormatException e) {
                showUpdateError("Points and Balance must be valid numbers");
                return;
            }

            if (updateBirthdatePicker.getValue() != null) {
                LocalDate birthDateLocal = updateBirthdatePicker.getValue();
                int age = Period.between(birthDateLocal, LocalDate.now()).getYears();
                if (age < 18) {
                    showUpdateError("User must be at least 18 years old");
                    return;
                }
                selectedUserForUpdate.setBirthdate(Date.from(updateBirthdatePicker.getValue()
                        .atStartOfDay(ZoneId.systemDefault()).toInstant()));
            }

            // Save to database
            userService.updateUser(selectedUserForUpdate);

            // Refresh table
            loadUsers();

            // Close dialog
            closeUpdateDialog();

        } catch (SQLException e) {
            showUpdateError(toFriendlyDatabaseErrorMessage(e));
            e.printStackTrace();
        } catch (RuntimeException e) {
            showAlert(Alert.AlertType.ERROR, "Unexpected error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private String toFriendlyDatabaseErrorMessage(SQLException e) {
        String message = e.getMessage() == null ? "" : e.getMessage();
        String sqlState = e.getSQLState() == null ? "" : e.getSQLState();
        String lower = message.toLowerCase();

        String duplicateValue = null;
        String keyName = null;

        java.util.regex.Matcher valueMatcher = java.util.regex.Pattern
                .compile("duplicate entry '([^']+)'", java.util.regex.Pattern.CASE_INSENSITIVE)
                .matcher(message);
        if (valueMatcher.find()) {
            duplicateValue = valueMatcher.group(1);
        }

        java.util.regex.Matcher keyMatcher = java.util.regex.Pattern
                .compile("for key '([^']+)'", java.util.regex.Pattern.CASE_INSENSITIVE)
                .matcher(message);
        if (keyMatcher.find()) {
            keyName = keyMatcher.group(1);
        }

        if (sqlState.startsWith("23") || lower.contains("duplicate entry")) {
            if ((keyName != null && keyName.equalsIgnoreCase("cin")) || lower.contains("key 'cin'") || lower.contains("key `cin`")) {
                return duplicateValue == null
                        ? "CIN already exists. Please choose a different CIN."
                        : "CIN already exists: " + duplicateValue;
            }
            if ((keyName != null && keyName.equalsIgnoreCase("email")) || lower.contains("key 'email'") || lower.contains("key `email`")) {
                return duplicateValue == null
                        ? "Email already exists. Please choose a different email."
                        : "Email already exists: " + duplicateValue;
            }
            if ((keyName != null && keyName.equalsIgnoreCase("numerocarte")) || (keyName != null && keyName.equalsIgnoreCase("numeroCarte")) ||
                    lower.contains("key 'numerocarte'") || lower.contains("key `numerocarte`") || lower.contains("key 'numeroCarte'") || lower.contains("key `numerocarte`")) {
                return duplicateValue == null
                        ? "Card number already exists. Please choose a different card number."
                        : "Card number already exists: " + duplicateValue;
            }
            if (duplicateValue != null) {
                return "This value already exists: " + duplicateValue;
            }
            return "This value already exists. Please choose a different value.";
        }

        return "Database error: " + message;
    }

    /**
     * Handle update cancel action
     */
    @FXML
    private void handleUpdateCancel() {
        closeUpdateDialog();
    }

    /**
     * Close update dialog
     */
    private void closeUpdateDialog() {
        if (updateCancelButton != null) {
            Stage stage = (Stage) updateCancelButton.getScene().getWindow();
            stage.close();
        }
    }

    /**
     * Show error in update dialog
     */
    private void showUpdateError(String message) {
        if (updateErrorLabel != null) {
            updateErrorLabel.setText(message);
            updateErrorLabel.setVisible(true);
        }
    }

    /**
     * Handle delete user action
     */
    private void handleDeleteUser(User user) {
        try {
            selectedUserForDelete = user;
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/DeleteConfirmDialog.fxml"));
            loader.setController(this);
            Parent root = loader.load();

            // Set user info
            if (deleteUserInfoLabel != null) {
                deleteUserInfoLabel.setText(user.getFirstName() + " " + user.getLastName() +
                        " (" + user.getEmail() + ")");
            }

            // Show dialog
            Stage dialogStage = new Stage();
            dialogStage.setTitle("Confirm Deletion");
            dialogStage.initModality(Modality.APPLICATION_MODAL);
            dialogStage.setScene(new Scene(root));
            dialogStage.setResizable(false);
            dialogStage.showAndWait();

        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Failed to open delete dialog: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Handle delete confirm action
     */
    @FXML
    private void handleDeleteConfirm() {
        if (selectedUserForDelete == null)
            return;

        try {
            userService.deleteUser(selectedUserForDelete.getId());

            // Refresh table
            loadUsers();

            // Close dialog
            closeDeleteDialog();

        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Failed to delete user: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Handle delete cancel action
     */
    @FXML
    private void handleDeleteCancel() {
        closeDeleteDialog();
    }

    /**
     * Close delete dialog
     */
    private void closeDeleteDialog() {
        if (deleteCancelButton != null) {
            Stage stage = (Stage) deleteCancelButton.getScene().getWindow();
            stage.close();
        }
    }

    /**
     * Handle export to PDF action
     */
    @FXML
    private void handleExportPdf() {
        javafx.stage.FileChooser fileChooser = new javafx.stage.FileChooser();
        fileChooser.setTitle("Save PDF Report");
        fileChooser.getExtensionFilters().add(
                new javafx.stage.FileChooser.ExtensionFilter("PDF Files", "*.pdf"));
        fileChooser.setInitialFileName("finovate_users_report_" + 
                new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date()) + ".pdf");

        java.io.File file = fileChooser.showSaveDialog(
                exportPdfButton != null ? (Stage) exportPdfButton.getScene().getWindow() : null);

        if (file != null) {
            try {
                PDFExportService pdfService = new PDFExportService();
                List<User> usersToExport = userService.getAllUsers();
                pdfService.exportUsersToPDF(usersToExport, file.getAbsolutePath());

                showAlert(Alert.AlertType.INFORMATION, 
                        "PDF report generated successfully!\nLocation: " + file.getAbsolutePath());
            } catch (Exception e) {
                showAlert(Alert.AlertType.ERROR, "Failed to generate PDF: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    /**
     * Handle logout action
     */
    @FXML
    private void handleLogout() {
        userService.logout();
        org.esprit.finovate.utils.RememberMeService.clearCredentials();

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Login.fxml"));
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

    /**
     * Show alert dialog
     */
    private void showAlert(Alert.AlertType type, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(type == Alert.AlertType.ERROR ? "Error" : "Information");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
