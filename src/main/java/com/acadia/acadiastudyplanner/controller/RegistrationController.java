package com.acadia.acadiastudyplanner.controller;

import com.acadia.acadiastudyplanner.data.DatabaseManager;
import javafx.animation.PauseTransition;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
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

/**
 * Controller for the registration-view.fxml. Handles user input validation,
 * database insertion for new users, and navigation back to the login screen.
 */
public class RegistrationController implements Initializable {

    @FXML private TextField nameField; // NEW: Field for the user's display name
    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private PasswordField confirmPasswordField;
    @FXML private Label messageLabel;
    @FXML private VBox logoContainer;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // Logo setup for visual consistency
        try {
            Image logo = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/com/acadia/acadiastudyplanner/images/lightmode.png")));
            ImageView logoView = new ImageView(logo);
            logoView.setFitHeight(180);
            logoView.setPreserveRatio(true);
            logoContainer.getChildren().add(0, logoView);
        } catch (Exception e) {
            System.err.println("Error loading logo image: " + e.getMessage());
            Label fallbackTitle = new Label("ACADIA");
            fallbackTitle.getStyleClass().add("app-title-login");
            logoContainer.getChildren().add(0, fallbackTitle);
        }
    }

    /**
     * Handles the click action of the "Register" button.
     * Performs validation and attempts to insert the new user into the database.
     */
    @FXML
    private void handleRegister() {
        String displayName = nameField.getText().trim();
        String username = usernameField.getText().trim();
        String password = passwordField.getText();
        String confirmPassword = confirmPasswordField.getText();

        // 1. Basic Field Validation (No password length restriction)
        if (displayName.isEmpty() || username.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
            showMessage("Please fill in all fields.", "error-label");
            return;
        }

        if (!password.equals(confirmPassword)) {
            showMessage("Passwords do not match.", "error-label");
            return;
        }

        // Simple check to prevent problematic usernames
        if (username.contains(" ")) {
            showMessage("Username cannot contain spaces.", "error-label");
            return;
        }

        try {
            // 2. Database Check
            if (DatabaseManager.userExists(username)) {
                showMessage("Username already taken.", "error-label");
            } else {
                // 3. Database Insert (Now includes displayName)
                DatabaseManager.insertUser(username, password, displayName);
                showMessage("Registration successful! Returning to login...", "success-label");

                // Clear fields and automatically switch back after a short delay
                clearFields();
                PauseTransition pause = new PauseTransition(Duration.seconds(2));
                pause.setOnFinished(event -> handleSwitchToLogin());
                pause.play();
            }
        } catch (SQLException e) {
            System.err.println("Database error during registration: " + e.getMessage());
            showMessage("Registration failed due to a database error.", "error-label");
        }
    }

    /**
     * Handles the action of the "Already have an account? Sign In" hyperlink.
     * Switches the application view back to the login-view.fxml.
     */
    @FXML
    private void handleSwitchToLogin() {
        try {
            Parent loginRoot = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("/com/acadia/acadiastudyplanner/view/login-view.fxml")));
            Stage stage = (Stage) usernameField.getScene().getWindow();
            stage.getScene().setRoot(loginRoot);
            stage.setTitle("Acadia Login");
            stage.centerOnScreen();
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Failed to load login view.");
        }
    }

    private void showMessage(String message, String styleClass) {
        messageLabel.setText(message);
        messageLabel.getStyleClass().setAll(styleClass);
        messageLabel.setVisible(true);
        messageLabel.setManaged(true);

        // Hide error messages automatically after a delay
        if (styleClass.equals("error-label")) {
            PauseTransition visiblePause = new PauseTransition(Duration.seconds(4));
            visiblePause.setOnFinished(event -> {
                messageLabel.setVisible(false);
                messageLabel.setManaged(false);
            });
            visiblePause.play();
        }
    }

    private void clearFields() {
        nameField.clear(); // Clears the new name field
        usernameField.clear();
        passwordField.clear();
        confirmPasswordField.clear();
    }
}
