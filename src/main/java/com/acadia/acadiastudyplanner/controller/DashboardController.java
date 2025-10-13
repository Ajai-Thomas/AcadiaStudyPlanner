package com.acadia.acadiastudyplanner.controller;

import com.acadia.acadiastudyplanner.Main;
import javafx.animation.FadeTransition;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ToggleButton;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

import java.io.IOException;
import java.net.URL;
import java.util.Objects;
import java.util.ResourceBundle;

public class DashboardController implements Initializable {

    @FXML private AnchorPane schedulePane;
    @FXML private BorderPane mainBorderPane;
    @FXML private VBox dashboardContent;
    @FXML private VBox navigationBox;
    @FXML private ToggleButton themeToggle;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        if (schedulePane != null) {
            populateScheduleGrid();
        }
        themeToggle.selectedProperty().addListener((obs, wasSelected, isSelected) -> {
            Scene scene = Main.getScene();
            if (scene == null) return;
            String darkThemePath = Objects.requireNonNull(getClass().getResource("/com/acadia/acadiastudyplanner/css/dark-theme.css")).toExternalForm();
            if (isSelected) {
                scene.getStylesheets().add(darkThemePath);
            } else {
                scene.getStylesheets().remove(darkThemePath);
            }
        });
    }

    @FXML
    private void handleNavigation(ActionEvent event) {
        Button clickedButton = (Button) event.getSource();
        updateActiveButton(clickedButton);
        String fxmlFile = "";
        switch (clickedButton.getText()) {
            case "Dashboard":
                setView(dashboardContent);
                return;
            case "My Subjects":
                fxmlFile = "subjects-view.fxml";
                break;
            case "Progress":
                fxmlFile = "progress-view.fxml";
                break;
            case "Settings": // Added case for Settings
                fxmlFile = "settings-view.fxml";
                break;
            default:
                System.out.println(clickedButton.getText() + " View not implemented yet.");
                updateActiveButton(null);
                return;
        }
        try {
            String path = "/com/acadia/acadiastudyplanner/view/" + fxmlFile;
            Parent view = FXMLLoader.load(Objects.requireNonNull(getClass().getResource(path)));
            setView(view);
        } catch (IOException | NullPointerException e) {
            e.printStackTrace();
        }
    }

    private void setView(Node node) {
        if (mainBorderPane.getCenter() != node) {
            mainBorderPane.setCenter(node);
            FadeTransition ft = new FadeTransition(Duration.millis(300), node);
            ft.setFromValue(0.0);
            ft.setToValue(1.0);
            ft.play();
        }
    }

    private void updateActiveButton(Button activeButton) {
        for (Node node : navigationBox.getChildren()) {
            if (node instanceof Button) {
                node.getStyleClass().remove("nav-button-active");
                node.getStyleClass().add("nav-button");
            }
        }
        if (activeButton != null) {
            activeButton.getStyleClass().remove("nav-button");
            activeButton.getStyleClass().add("nav-button-active");
        } else {
            // Default to dashboard if something goes wrong
            if (!navigationBox.getChildren().isEmpty()) {
                ((Button) navigationBox.getChildren().get(0)).fire();
            }
        }
    }

    private void populateScheduleGrid() {
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        AnchorPane.setTopAnchor(grid, 0.0);
        AnchorPane.setBottomAnchor(grid, 0.0);
        AnchorPane.setLeftAnchor(grid, 0.0);
        AnchorPane.setRightAnchor(grid, 0.0);
        String[] days = {"MON", "TUE", "WED", "THU", "FRI", "SAT", "SUN"};
        for (int i = 0; i < days.length; i++) {
            grid.add(new Label(days[i]) {{ getStyleClass().add("grid-day-header"); }}, i, 0);
        }
        grid.add(createScheduleItem("Data Structures", "9-11 AM", "red-item"), 0, 1);
        grid.add(createScheduleItem("Calculus II", "1-3 PM", "green-item"), 0, 2);
        grid.add(createScheduleItem("Software Eng.", "10-12 PM", "green-item"), 1, 1);
        grid.add(createScheduleItem("Study Break", "12-1 PM", "gray-item"), 2, 1);
        grid.add(createScheduleItem("Data Structures", "3-5 PM", "red-item"), 2, 2);
        grid.add(createScheduleItem("Calculus II", "9-11 AM", "green-item"), 3, 1);
        grid.add(createScheduleItem("Review Session", "6-8 PM", "red-item"), 4, 1);
        schedulePane.getChildren().add(grid);
    }

    private VBox createScheduleItem(String title, String time, String styleClass) {
        VBox itemBox = new VBox(-2);
        itemBox.setPadding(new Insets(8, 12, 8, 12));
        itemBox.getStyleClass().addAll("schedule-item", styleClass);
        Label titleLabel = new Label(title);
        titleLabel.getStyleClass().add("item-title");
        Label timeLabel = new Label(time);
        timeLabel.getStyleClass().add("item-time");
        itemBox.getChildren().addAll(titleLabel, timeLabel);
        return itemBox;
    }
}

