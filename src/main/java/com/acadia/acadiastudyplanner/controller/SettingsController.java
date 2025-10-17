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