package com.acadia.acadiastudyplanner.controller;

import com.acadia.acadiastudyplanner.data.DatabaseManager;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.TextField;
import java.net.URL;
import java.sql.SQLException;
import java.util.Map;
import java.util.ResourceBundle;

/**
 * Controller for the settings-view.fxml.
 */
public class SettingsController implements Initializable {

    @FXML private TextField userNameField;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        loadUserProfile();
        System.out.println("Settings Controller initialized.");
    }

    private void loadUserProfile() {
        if (userNameField != null) {
            int userId = LoginController.currentUserID;
            if (userId != -1) {
                try {
                    Map<String, Object> prefs = DatabaseManager.loadUserPreferences(userId);
                    String displayName = (String) prefs.get("DisplayName");

                    userNameField.setText(displayName);
                } catch (SQLException e) {
                    System.err.println("Failed to load user profile data: " + e.getMessage());
                    userNameField.setText("Database Error");
                }
            } else {
                userNameField.setText("Guest Profile");
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
        int userId = LoginController.currentUserID;

        try {
            DatabaseManager.updateDisplayName(userId, newName);
            showInfo("Success", "Display Name updated successfully!");
        } catch (SQLException e) {
            showError("Database Error", "Failed to update display name: " + e.getMessage());
        }
    }

    // Preference and Availability methods removed.

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
}