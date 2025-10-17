package com.acadia.acadiastudyplanner.controller;

import com.acadia.acadiastudyplanner.Main;
import com.acadia.acadiastudyplanner.data.DatabaseManager;
import javafx.animation.PauseTransition;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.util.Objects;
import java.util.ResourceBundle;

public class LoginController implements Initializable {

    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private Label errorLabel;
    @FXML private VBox logoContainer;

    // A static variable to hold the logged-in user's ID (Crucial for later persistence tasks)
    public static int currentUserID = -1;
    public static String currentUsername = null;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // Logo setup (increased size)
        try {
            Image logo = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/com/acadia/acadiastudyplanner/images/lightmode.png")));
            ImageView logoView = new ImageView(logo);
            logoView.setFitHeight(300);
            logoView.setPreserveRatio(true);
            logoContainer.getChildren().add(0, logoView);
        } catch (Exception e) {
            System.err.println("Error loading logo image: " + e.getMessage());
            Label fallbackTitle = new Label("ACADIA");
            fallbackTitle.getStyleClass().add("app-title-login");
            logoContainer.getChildren().add(0, fallbackTitle);
        }

        // --- DATABASE SETUP FOR DEFAULT USER ---
        try {
            // FIX: The insertUser method now requires a third argument (displayName).
            if (!DatabaseManager.userExists("user")) {
                DatabaseManager.insertUser("user", "pass", "Acadia Student"); // Added Display Name
                System.out.println("Default user ('user'/'pass' with DisplayName 'Acadia Student') created in DB.");
            }
        } catch (SQLException e) {
            System.err.println("Error checking/creating default user: " + e.getMessage());
        }
    }

    @FXML
    private void handleLogin() {
        String username = usernameField.getText().trim();
        String password = passwordField.getText();

        int loggedInId = -1;
        try {
            // DATABASE VALIDATION & GET USER ID
            loggedInId = DatabaseManager.validateAndGetUserID(username, password);
        } catch (SQLException e) {
            System.err.println("Login database error: " + e.getMessage());
            errorLabel.setText("Database connection error.");
            errorLabel.setVisible(true);
            return;
        }

        if (loggedInId != -1) {
            currentUserID = loggedInId; // Set the static ID for session management
            currentUsername = username; // Set the static username
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

    // --- METHOD FOR SWITCHING TO REGISTRATION ---
    @FXML
    private void handleSwitchToRegister() {
        try {
            Parent registerRoot = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("/com/acadia/acadiastudyplanner/view/registration-view.fxml")));
            Stage stage = (Stage) usernameField.getScene().getWindow();
            stage.getScene().setRoot(registerRoot);
            stage.setTitle("Acadia Registration");
            stage.centerOnScreen();
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Failed to load registration view.");
        }
    }

    private void switchToDashboard() {
        try {
            Parent dashboardRoot = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("/com/acadia/acadiastudyplanner/view/dashboard-view.fxml")));
            Stage stage = (Stage) usernameField.getScene().getWindow();
            Scene dashboardScene = new Scene(dashboardRoot, 1100, 700);
            dashboardScene.getStylesheets().addAll(stage.getScene().getStylesheets());
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