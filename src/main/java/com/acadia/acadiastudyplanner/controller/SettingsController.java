package com.acadia.acadiastudyplanner.controller;

import com.acadia.acadiastudyplanner.data.DatabaseManager;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TextField;
import java.net.URL;
import java.sql.SQLException;
import java.util.Map;
import java.util.Optional;
import java.util.ResourceBundle;

public class SettingsController implements Initializable {

    @FXML private TextField userNameField;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        loadUserProfile();
    }

    private void loadUserProfile() {
        if (userNameField != null) {
            try {
                Map<String, Object> prefs = DatabaseManager.loadUserPreferences(LoginController.currentUserID);
                userNameField.setText((String) prefs.get("DisplayName"));
            } catch (SQLException e) {
                userNameField.setText("Database Error");
            }
        }
    }

    @FXML
    private void handleUpdateName() {
        String newName = userNameField.getText().trim();
        if (newName.isEmpty()) {
            showError("Input Error", "Display Name cannot be empty.");
            return;
        }
        try {
            DatabaseManager.updateDisplayName(LoginController.currentUserID, newName);
            showInfo("Success", "Display Name updated successfully!");
        } catch (SQLException e) {
            showError("Database Error", "Failed to update display name: " + e.getMessage());
        }
    }

    @FXML
    private void handleExportData() {
        // This is where you would open a FileChooser to select a save location.
        System.out.println("ACTION: Export Data button clicked. (Placeholder)");
        showAlert(Alert.AlertType.INFORMATION, "Data Export", "Subject and progress data has been prepared for export.");
        // TODO: Implement FileChooser and actual data serialization (e.g., to JSON/XML).
    }

    @FXML
    private void handleImportData() {
        // This is where you would open a FileChooser to select a file to load.
        System.out.println("ACTION: Import Data button clicked. (Placeholder)");
        showAlert(Alert.AlertType.INFORMATION, "Data Import", "The import process has been initiated. Please select a file.");
        // TODO: Implement FileChooser and actual data deserialization.
    }

    @FXML
    private void handleResetAllData() {
        System.out.println("ACTION: Reset All Data button clicked.");

        Optional<ButtonType> result = showAlert(Alert.AlertType.CONFIRMATION, "Confirm Data Reset",
                "WARNING: This will permanently delete ALL subjects and progress data. Are you absolutely sure?");

        if (result.isPresent() && result.get() == ButtonType.OK) {
            System.out.println("DATA RESET CONFIRMED. Deleting all data...");
            // TODO: Implement actual data deletion/reset logic across all models here.
            showAlert(Alert.AlertType.INFORMATION, "Success", "All application data has been permanently deleted.");
        } else {
            System.out.println("Data reset cancelled by user.");
        }
    }

    private void showError(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    private void showInfo(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
    
    private Optional<ButtonType> showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        return alert.showAndWait();
    }
}