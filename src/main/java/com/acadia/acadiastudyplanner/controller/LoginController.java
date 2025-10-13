package com.acadia.acadiastudyplanner.controller;

import com.acadia.acadiastudyplanner.Main;
import javafx.animation.PauseTransition;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.IOException;
import java.util.Objects;

public class LoginController {

    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private Label errorLabel;

    @FXML
    private void handleLogin() {
        String username = usernameField.getText();
        String password = passwordField.getText();

        if ("user".equals(username) && "pass".equals(password)) {
            errorLabel.setVisible(false);
            switchToDashboard();
        } else {
            errorLabel.setText("Invalid username or password.");
            errorLabel.setVisible(true);
            PauseTransition visiblePause = new PauseTransition(Duration.seconds(3));
            visiblePause.setOnFinished(event -> errorLabel.setVisible(false));
            visiblePause.play();
        }
    }

    private void switchToDashboard() {
        try {
            Parent dashboardRoot = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("/com/acadia/acadiastudyplanner/view/dashboard-view.fxml")));
            Stage stage = (Stage) usernameField.getScene().getWindow();

            // Create a new scene for the dashboard
            Scene dashboardScene = new Scene(dashboardRoot, 1100, 700);

            // Pass the stylesheets from the login scene to the new dashboard scene
            dashboardScene.getStylesheets().addAll(stage.getScene().getStylesheets());

            // --- THIS IS THE FIX ---
            // Update the static scene reference in the Main class to point to the new dashboard scene.
            Main.scene = dashboardScene;

            stage.setScene(dashboardScene);
            stage.setTitle("Acadia - Personalized Study Planner");
            stage.setResizable(true);
            stage.setMinWidth(1000);
            stage.setMinHeight(650);
            stage.centerOnScreen();

        } catch (IOException e) {
            e.printStackTrace();
            errorLabel.setText("Error: Could not load the main application.");
            errorLabel.setVisible(true);
        }
    }
}

