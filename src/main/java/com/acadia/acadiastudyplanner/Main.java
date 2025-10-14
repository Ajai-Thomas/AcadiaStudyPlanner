package com.acadia.acadiastudyplanner;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Objects;

public class Main extends Application {

    public static Scene scene;

    @Override
    public void start(Stage primaryStage) {
        try {
            Parent root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("/com/acadia/acadiastudyplanner/view/login-view.fxml")));
            scene = new Scene(root, 500, 450);

            // Add Google Fonts and main stylesheet programmatically
            String interFont = "https://fonts.googleapis.com/css2?family=Inter:wght@300;400;500;600;700;800&display=swap";
            String mainStylesheet = Objects.requireNonNull(getClass().getResource("/com/acadia/acadiastudyplanner/css/styles.css")).toExternalForm();
            scene.getStylesheets().addAll(interFont, mainStylesheet);

            primaryStage.setTitle("Acadia Login");
            primaryStage.setScene(scene);
            primaryStage.setResizable(false);
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

