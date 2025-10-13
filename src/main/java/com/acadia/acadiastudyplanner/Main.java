package com.acadia.acadiastudyplanner;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Objects;

public class Main extends Application {

    private static Scene scene;

    @Override
    public void start(Stage primaryStage) {
        try {
            Parent root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("/com/acadia/acadiastudyplanner/view/dashboard-view.fxml")));
            scene = new Scene(root, 1100, 700);

            // Programmatically add the main stylesheet
            String mainStylesheet = Objects.requireNonNull(getClass().getResource("/com/acadia/acadiastudyplanner/css/styles.css")).toExternalForm();
            scene.getStylesheets().add(mainStylesheet);

            primaryStage.setTitle("Acadia - Personalized Study Planner");
            primaryStage.setScene(scene);
            primaryStage.setMinWidth(1000);
            primaryStage.setMinHeight(650);
            primaryStage.show();
        } catch (IOException | NullPointerException e) {
            e.printStackTrace();
            System.err.println("Failed to load the main FXML file or CSS.");
        }
    }

    public static Scene getScene() {
        return scene;
    }

    public static void main(String[] args) {
        launch(args);
    }
}

