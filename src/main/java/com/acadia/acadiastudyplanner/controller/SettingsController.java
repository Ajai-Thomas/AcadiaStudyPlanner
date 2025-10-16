package com.acadia.acadiastudyplanner.controller;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TextField;
import java.net.URL;
import java.util.Optional;
import java.util.ResourceBundle;

/**
 * Controller for the settings-view.fxml.
 * This handles user preferences and data management.
 */
public class SettingsController implements Initializable {

    @FXML private TextField studentNameField; // FIX 1: Linked FXML field

    private String studentName = "Student"; // Simple placeholder for profile data

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // FIX 2: Load initial value (though in a real app, this would be loaded from persistence)
        studentNameField.setText(studentName);
        System.out.println("Settings Controller initialized.");
    }

    // --- User Profile Handler ---

    @FXML
    private void handleSaveProfile() {
        String newName = studentNameField.getText().trim();
        if (!newName.isEmpty()) {
            studentName = newName;
            System.out.println("ACTION: Profile updated to " + studentName);
            showAlert(Alert.AlertType.INFORMATION, "Profile Updated", "Your student name has been saved.");
        } else {
            showAlert(Alert.AlertType.WARNING, "Input Error", "Student name cannot be empty.");
        }
    }

    // --- Data Management Handlers ---

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

    // --- Danger Zone Handler ---

    @FXML
    private void handleResetAllData() {
        System.out.println("ACTION: Reset All Data button clicked.");

        // FIX 3: Confirmation Dialog for destructive action
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

    // --- Utility Method ---

    private Optional<ButtonType> showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        return alert.showAndWait();
    }
}