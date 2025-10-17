package com.acadia.acadiastudyplanner.controller;

import com.acadia.acadiastudyplanner.Main;
import com.acadia.acadiastudyplanner.data.DatabaseManager;
import com.acadia.acadiastudyplanner.model.StudyTask;
import javafx.animation.FadeTransition;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.ToggleButton;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class DashboardController implements Initializable {

    @FXML private AnchorPane schedulePane;
    @FXML private BorderPane mainBorderPane;
    @FXML private VBox dashboardContent;
    @FXML private VBox navigationBox;
    @FXML private ToggleButton themeToggle;
    @FXML private VBox logoContainer;
    @FXML private Label greetingLabel;
    @FXML private ListView<StudyTask> pendingTasksListView;
    @FXML private Button completeButton;
    @FXML private Button generateScheduleButton;

    private final ObservableList<StudyTask> pendingTasks = FXCollections.observableArrayList();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        loadDashboardLogo();
        loadGreeting();
        pendingTasksListView.setItems(pendingTasks);
        pendingTasksListView.getSelectionModel().selectedItemProperty().addListener(
                (obs, oldV, newV) -> completeButton.setDisable(newV == null)
        );
        setupTaskListView();
        loadPendingTasks();
        if (schedulePane != null) {
            populateScheduleGrid();
        }
        themeToggle.selectedProperty().addListener((obs, wasSelected, isSelected) -> updateTheme(isSelected));
    }

    private void setupTaskListView() {
        pendingTasksListView.setCellFactory(lv -> new javafx.scene.control.ListCell<StudyTask>() {
            @Override
            protected void updateItem(StudyTask item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    String subjectName = item.getSubjectName() != null ? item.getSubjectName() : "No Subject";
                    String display = String.format("[%s] %s: %s (Due: %s | Est: %s)",
                            item.getTaskType(),
                            item.getTitle(),
                            subjectName,
                            item.getDeadlineDate(),
                            item.getDurationEstimate());
                    setText(display);
                    setStyle(item.getStatus().equals("Pending") ? "-fx-font-weight: bold; -fx-text-fill: #ffa000;" : "-fx-font-weight: normal; -fx-text-fill: #999999;");
                }
            }
        });
    }

    private void loadPendingTasks() {
        int userId = LoginController.currentUserID;
        if (userId == -1) return;
        try {
            List<StudyTask> filteredTasks = DatabaseManager.loadTasksForProgress(userId).stream()
                    .filter(task -> task.getStatus().equalsIgnoreCase("Pending"))
                    .collect(Collectors.toList());
            pendingTasks.setAll(filteredTasks);
            if (pendingTasks.isEmpty()) {
                pendingTasksListView.setPlaceholder(new Label("No outstanding academic work defined."));
            }
        } catch (SQLException e) {
            showAlert("Database Error", "Failed to load pending workload.");
        }
    }

    @FXML
    private void handleMarkComplete() {
        StudyTask selectedTask = pendingTasksListView.getSelectionModel().getSelectedItem();
        if (selectedTask == null) return;
        try {
            DatabaseManager.updateTaskStatus(selectedTask.getId(), "Completed");
            pendingTasks.remove(selectedTask);
            showInfo("Success", String.format("Task '%s' marked as Complete!", selectedTask.getTitle()));
        } catch (SQLException e) {
            showAlert("Database Error", "Failed to mark task as complete: " + e.getMessage());
        }
    }

    @FXML
    private void handleGenerateSchedule() {
        if (generateScheduleButton != null) {
            generateScheduleButton.setDisable(true);
            generateScheduleButton.setText("Generating...");
        }
        new Thread(() -> {
            try {
                Thread.sleep(1500); // Simulate network delay
                javafx.application.Platform.runLater(() -> {
                    if (generateScheduleButton != null) {
                        generateScheduleButton.setText("Generate Schedule");
                        generateScheduleButton.setDisable(false);
                    }
                    showInfo("Success", "The optimized weekly schedule has been generated!");
                });
            } catch (Exception e) {
                javafx.application.Platform.runLater(() -> {
                    if (generateScheduleButton != null) {
                        generateScheduleButton.setText("Generate Schedule");
                        generateScheduleButton.setDisable(false);
                    }
                    showAlert("Scheduling Failed", "Error generating schedule: " + e.getMessage());
                });
            }
        }).start();
    }

    @FXML
    private void handleManagePreferences() {
        try {
            FXMLLoader loader = new FXMLLoader(Objects.requireNonNull(getClass().getResource("/com/acadia/acadiastudyplanner/view/preferences-dialog.fxml")));
            Stage stage = new Stage();
            stage.setTitle("AI Scheduling Preferences & Availability");
            stage.setScene(new Scene(loader.load(), 460, 650));
            stage.initOwner(mainBorderPane.getScene().getWindow());
            stage.initModality(Modality.WINDOW_MODAL);
            stage.setResizable(false);
            stage.getScene().getStylesheets().addAll(mainBorderPane.getScene().getStylesheets());
            stage.showAndWait();
            loadGreeting();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleNewTask() {
        try {
            FXMLLoader loader = new FXMLLoader(Objects.requireNonNull(getClass().getResource("/com/acadia/acadiastudyplanner/view/new-task-dialog.fxml")));
            Stage stage = new Stage();
            stage.setTitle("Define New Academic Work");
            stage.setScene(new Scene(loader.load(), 420, 550));
            stage.initOwner(mainBorderPane.getScene().getWindow());
            stage.initModality(Modality.WINDOW_MODAL);
            stage.setResizable(false);
            stage.getScene().getStylesheets().addAll(mainBorderPane.getScene().getStylesheets());
            stage.showAndWait();
            loadPendingTasks();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleNavigation(ActionEvent event) {
        Button clickedButton = (Button) event.getSource();
        updateActiveButton(clickedButton);
        String fxmlFile = "";
        switch (clickedButton.getText()) {
            case "Dashboard": setView(dashboardContent); loadPendingTasks(); return;
            case "My Subjects": fxmlFile = "subjects-view.fxml"; break;
            case "Progress": fxmlFile = "progress-view.fxml"; break;
            case "Settings": fxmlFile = "settings-view.fxml"; break;
            default: updateActiveButton(null); return;
        }
        try {
            setView(FXMLLoader.load(Objects.requireNonNull(getClass().getResource("/com/acadia/acadiastudyplanner/view/" + fxmlFile))));
        } catch (IOException | NullPointerException e) {
            showAlert("Navigation Error", "Could not load the '" + clickedButton.getText() + "' page.");
            e.printStackTrace();
        }
    }

    private void loadGreeting() {
        if (greetingLabel != null) {
            try {
                Map<String, Object> prefs = DatabaseManager.loadUserPreferences(LoginController.currentUserID);
                greetingLabel.setText("Hello, " + prefs.get("DisplayName") + "!");
            } catch (SQLException e) {
                greetingLabel.setText("Hello, Student!");
            }
        }
    }

    private void loadDashboardLogo() {
        try {
            ImageView logoView = new ImageView(new Image(Objects.requireNonNull(getClass().getResourceAsStream("/com/acadia/acadiastudyplanner/images/darkmode.png"))));
            logoView.setFitHeight(120);
            logoView.setPreserveRatio(true);
            logoContainer.getChildren().add(0, logoView);
        } catch (Exception e) {
            logoContainer.getChildren().add(0, new Label("ACADIA") {{ getStyleClass().add("app-title"); }});
        }
    }

    private void updateTheme(boolean isDarkMode) {
        Scene scene = Main.getScene();
        if (scene == null) return;
        String darkThemePath = Objects.requireNonNull(getClass().getResource("/com/acadia/acadiastudyplanner/css/dark-theme.css")).toExternalForm();
        boolean isDarkCurrentlyApplied = scene.getStylesheets().contains(darkThemePath);
        if (isDarkMode && !isDarkCurrentlyApplied) {
            scene.getStylesheets().add(darkThemePath);
        } else if (!isDarkMode && isDarkCurrentlyApplied) {
            scene.getStylesheets().remove(darkThemePath);
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
        navigationBox.getChildren().forEach(node -> {
            if (node instanceof Button) {
                node.getStyleClass().remove("nav-button-active");
                node.getStyleClass().add("nav-button");
            }
        });
        if (activeButton != null) {
            activeButton.getStyleClass().remove("nav-button");
            activeButton.getStyleClass().add("nav-button-active");
        } else {
            ((Button) navigationBox.getChildren().get(0)).fire();
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
        schedulePane.getChildren().add(grid);
    }

    private VBox createScheduleItem(String title, String time, String styleClass) {
        VBox itemBox = new VBox(-2);
        itemBox.setPadding(new Insets(8, 12, 8, 12));
        itemBox.getStyleClass().addAll("schedule-item", styleClass);
        itemBox.getChildren().addAll(new Label(title) {{ getStyleClass().add("item-title"); }}, new Label(time) {{ getStyleClass().add("item-time"); }});
        return itemBox;
    }

    private void showAlert(String title, String content) {
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