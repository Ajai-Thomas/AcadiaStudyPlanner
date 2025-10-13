package com.acadia.acadiastudyplanner;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Objects;

public class Main extends Application {

    // Changed from 'private' to 'public' to allow access from other controllers
    public static Scene scene;

    @Override
    public void start(Stage primaryStage) {
        try {
            // Start with the login view instead of the dashboard
            Parent root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("/com/acadia/acadiastudyplanner/view/login-view.fxml")));
            scene = new Scene(root, 500, 450); // Set an appropriate size for the login window

            // Programmatically add the main stylesheet
            String mainStylesheet = Objects.requireNonNull(getClass().getResource("/com/acadia/acadiastudyplanner/css/styles.css")).toExternalForm();
            scene.getStylesheets().add(mainStylesheet);

            primaryStage.setTitle("Acadia Login");
            primaryStage.setScene(scene);
            primaryStage.setResizable(false); // Login window shouldn't be resizable
            primaryStage.show();

        } catch (IOException | NullPointerException e) {
            e.printStackTrace();
            System.err.println("Failed to load the initial FXML file or CSS.");
        }
    }

    public static Scene getScene() {
        return scene;
    }

    public static void main(String[] args) {
        launch(args);
    }
}

