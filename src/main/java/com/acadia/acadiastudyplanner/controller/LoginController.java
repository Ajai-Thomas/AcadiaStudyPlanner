package com.acadia.acadiastudyplanner.controller;

import com.acadia.acadiastudyplanner.Main;
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
import java.util.Objects;
import java.util.ResourceBundle;

public class LoginController implements Initializable {

    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private Label errorLabel;
    @FXML private VBox logoContainer;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        try {
            Image logo = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/com/acadia/acadiastudyplanner/images/lightmode.png")));
            ImageView logoView = new ImageView(logo);
            // Increased the logo height substantially to 200
            logoView.setFitHeight(200);
            logoView.setPreserveRatio(true);
            logoContainer.getChildren().add(0, logoView);
        } catch (Exception e) {
            System.err.println("Error loading logo image: " + e.getMessage());
            Label fallbackTitle = new Label("ACADIA");
            fallbackTitle.getStyleClass().add("app-title-login");
            logoContainer.getChildren().add(0, fallbackTitle);
        }
    }

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

