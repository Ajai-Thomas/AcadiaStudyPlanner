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
import com.acadia.acadiastudyplanner.service.SchedulingService; // Added for context/completeness (though implementation is mocked)

import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.util.List;
import java.util.Objects;
import java.util.ResourceBundle;
import java.util.stream.Collectors;
import javafx.scene.control.Label; // Explicitly import Label
import javafx.scene.layout.Priority; // Explicitly import Priority
import javafx.geometry.HPos; // Explicitly import HPos
import javafx.geometry.VPos; // Explicitly import VPos
import java.util.Map; // Explicitly import Map

public class DashboardController implements Initializable {

    @FXML private AnchorPane schedulePane;
    @FXML private BorderPane mainBorderPane;
    @FXML private VBox dashboardContent;
    @FXML private VBox navigationBox;
    @FXML private ToggleButton themeToggle;
    @FXML private VBox logoContainer;
    @FXML private Label greetingLabel;

    // Fields required for the "Pending Workload" FXML section
    @FXML private ListView<StudyTask> pendingTasksListView;
    @FXML private Button completeButton;
    @FXML private Button generateScheduleButton; // Required for AI trigger

    private final ObservableList<StudyTask> pendingTasks = FXCollections.observableArrayList();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        loadDashboardLogo();
        loadGreeting();

        // Setup pending tasks list logic
        pendingTasksListView.setItems(pendingTasks);
        pendingTasksListView.getSelectionModel().selectedItemProperty().addListener(
                (obs, oldV, newV) -> completeButton.setDisable(newV == null)
        );
        setupTaskListView();
        loadPendingTasks();

        if (schedulePane != null) {
            populateScheduleGrid();
        }
        themeToggle.selectedProperty().addListener((obs, wasSelected, isSelected) -> {
            updateTheme(isSelected);
        });
    }

    // --- TASK COMPLETION AND LOADING LOGIC ---

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

                    if (item.getStatus().equals("Pending")) {
                        setStyle("-fx-font-weight: bold; -fx-text-fill: #ffa000;");
                    } else {
                        setStyle("-fx-font-weight: normal; -fx-text-fill: #999999;");
                    }
                }
            }
        });
    }

    private void loadPendingTasks() {
        int userId = LoginController.currentUserID;
        if (userId == -1) return;

        try {
            List<StudyTask> allTasks = DatabaseManager.loadTasksForProgress(userId);

            // Filter only tasks that are 'Pending'
            List<StudyTask> filteredTasks = allTasks.stream()
                    .filter(task -> task.getStatus().equalsIgnoreCase("Pending"))
                    .collect(Collectors.toList());

            pendingTasks.setAll(filteredTasks);

            if (pendingTasks.isEmpty()) {
                pendingTasksListView.setPlaceholder(new Label("No outstanding academic work defined."));
            }

        } catch (SQLException e) {
            System.err.println("Failed to load pending tasks: " + e.getMessage());
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

    // --- AI GENERATION LOGIC ---

    @FXML
    private void handleGenerateSchedule() {
        if (generateScheduleButton != null) {
            generateScheduleButton.setDisable(true);
            generateScheduleButton.setText("Generating...");
        }

        // Running API call asynchronously to prevent UI freeze
        new Thread(() -> {
            try {
                // Call the AI service (this parses and saves the schedule)
                // Note: Ensure your SchedulingService is fully implemented and API key is set.
                // SchedulingService service = new SchedulingService();
                // service.generateWeeklySchedule();

                // MOCK API SUCCESS for testing UI
                Thread.sleep(1500); // Simulate network delay

                // Update UI on JavaFX thread after success
                javafx.application.Platform.runLater(() -> {
                    // Re-enable and update button state
                    if (generateScheduleButton != null) {
                        generateScheduleButton.setText("Generate Schedule");
                        generateScheduleButton.setDisable(false);
                    }
                    // Load the newly generated schedule
                    // populateScheduleGrid();
                    showInfo("Success", "The optimized weekly schedule has been generated!");
                });

            } catch (Exception e) {
                // Handle errors on JavaFX thread
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

    // --- DIALOG OPENERS ---

    @FXML
    private void handleManagePreferences() {
        try {
            FXMLLoader loader = new FXMLLoader(Objects.requireNonNull(getClass().getResource("/com/acadia/acadiastudyplanner/view/preferences-dialog.fxml")));
            Parent parent = loader.load();

            Stage stage = new Stage();
            stage.setTitle("AI Scheduling Preferences & Availability");

            Scene scene = new Scene(parent, 460, 650);
            stage.setScene(scene);

            stage.initOwner(mainBorderPane.getScene().getWindow());
            stage.initModality(Modality.WINDOW_MODAL);
            stage.setResizable(false);

            stage.getScene().getStylesheets().addAll(mainBorderPane.getScene().getStylesheets());

            stage.showAndWait();
            loadGreeting();
        } catch (IOException e) {
            System.err.println("Failed to load Preferences Dialog.");
            e.printStackTrace();
        }
    }

    @FXML
    private void handleNewTask() {
        try {
            FXMLLoader loader = new FXMLLoader(Objects.requireNonNull(getClass().getResource("/com/acadia/acadiastudyplanner/view/new-task-dialog.fxml")));
            Parent parent = loader.load();

            Stage stage = new Stage();
            stage.setTitle("Define New Academic Work");

            Scene scene = new Scene(parent, 420, 550);
            stage.setScene(scene);

            stage.initOwner(mainBorderPane.getScene().getWindow());
            stage.initModality(Modality.WINDOW_MODAL);
            stage.setResizable(false);

            stage.getScene().getStylesheets().addAll(mainBorderPane.getScene().getStylesheets());

            stage.showAndWait();

            loadPendingTasks();

        } catch (IOException e) {
            System.err.println("Failed to load New Task Dialog.");
            e.printStackTrace();
        }
    }

    // --- NAVIGATION AND UI UTILITIES ---

    @FXML
    private void handleNavigation(ActionEvent event) {
        Button clickedButton = (Button) event.getSource();
        updateActiveButton(clickedButton);
        String fxmlFile = "";

        switch (clickedButton.getText()) {
            case "Dashboard":
                setView(dashboardContent);
                loadPendingTasks();
                return;
            case "My Subjects":
                fxmlFile = "subjects-view.fxml";
                break;
            case "Progress":
                fxmlFile = "progress-view.fxml";
                break;
            case "Settings":
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

        } catch (IOException e) {
            System.err.println("Failed to load view for: " + clickedButton.getText() + ". Check file path and FXML syntax.");
            e.printStackTrace();
            showAlert("Navigation Error", "Could not load the '" + clickedButton.getText() + "' page. Please ensure the FXML file exists.");

        } catch (NullPointerException e) {
            // Catches error if getResource returns null (file not found)
            showAlert("Resource Error", "The FXML file for the '" + clickedButton.getText() + "' page was not found at the expected path.");
            e.printStackTrace();
        }
    }

    private void loadGreeting() {
        if (greetingLabel != null) {
            int userId = LoginController.currentUserID;
            if (userId != -1) {
                try {
                    Map<String, Object> prefs = DatabaseManager.loadUserPreferences(userId);
                    String displayName = (String) prefs.get("DisplayName");
                    greetingLabel.setText("Hello, " + displayName + "!");
                } catch (SQLException e) {
                    System.err.println("Failed to load user display name: " + e.getMessage());
                    greetingLabel.setText("Hello, Student!");
                }
            } else {
                greetingLabel.setText("Hello, Guest!");
            }
        }
    }

    private void loadDashboardLogo() {
        // This method now ONLY loads and displays the dark mode logo.
        try {
            Image darkLogo = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/com/acadia/acadiastudyplanner/images/darkmode.png")));
            ImageView logoView = new ImageView(darkLogo);
            logoView.setFitHeight(120);
            logoView.setPreserveRatio(true);
            logoContainer.getChildren().add(0, logoView);
        } catch (Exception e) {
            System.err.println("Dashboard logo (darkmode.png) could not be loaded: " + e.getMessage());
            Label fallbackTitle = new Label("ACADIA");
            fallbackTitle.getStyleClass().add("app-title");
            logoContainer.getChildren().add(0, fallbackTitle);
        }
    }

    private void updateTheme(boolean isDarkMode) {
        Scene scene = Main.getScene();
        if (scene == null) return;
        String darkThemePath = Objects.requireNonNull(getClass().getResource("/com/acadia/acadiastudyplanner/css/dark-theme.css")).toExternalForm();

        // FIX: Reliable toggle logic
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
            // Re-fire the first button to return to Dashboard view if no button was selected
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
