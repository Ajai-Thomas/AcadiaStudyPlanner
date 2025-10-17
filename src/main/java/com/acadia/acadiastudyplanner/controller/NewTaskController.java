package com.acadia.acadiastudyplanner.controller;

import com.acadia.acadiastudyplanner.data.DatabaseManager;
import com.acadia.acadiastudyplanner.model.Subject;
import com.acadia.acadiastudyplanner.model.StudyTask;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.net.URL;
import java.sql.SQLException;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;
import java.util.ResourceBundle;

public class NewTaskController implements Initializable {

    // --- FXML Fields matching new schema ---
    @FXML private TextField titleField;
    @FXML private ComboBox<Subject> subjectComboBox;
    @FXML private ComboBox<String> taskTypeComboBox; // NEW
    @FXML private TextField durationField;          // NEW
    @FXML private DatePicker deadlinePicker;        // NEW
    @FXML private Label messageLabel;

    private final ObservableList<Subject> subjects = FXCollections.observableArrayList();
    private static final List<String> TASK_TYPES = List.of("Review", "Problem Set", "Essay Draft", "Reading", "Project Work", "Exam Prep");

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        loadSubjectsForComboBox();
        taskTypeComboBox.setItems(FXCollections.observableArrayList(TASK_TYPES));

        // Add a placeholder/default subject option
        subjectComboBox.getItems().add(0, new Subject(-1, "No Specific Subject", 0, ""));
        subjectComboBox.getSelectionModel().select(0);

        // Set default date format for deadline input
        deadlinePicker.setConverter(new javafx.util.StringConverter<>() {
            private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            @Override
            public String toString(java.time.LocalDate date) {
                return (date != null) ? dateFormatter.format(date) : "";
            }
            @Override
            public java.time.LocalDate fromString(String string) {
                return (string != null && !string.isEmpty()) ? java.time.LocalDate.parse(string, dateFormatter) : null;
            }
        });
    }

    private void loadSubjectsForComboBox() {
        int userId = LoginController.currentUserID;
        if (userId == -1) return;

        try {
            List<Subject> loadedSubjects = DatabaseManager.loadSubjects(userId);
            subjects.addAll(loadedSubjects);
            subjectComboBox.setItems(subjects);
        } catch (SQLException e) {
            System.err.println("Failed to load subjects for task dialog: " + e.getMessage());
            messageLabel.setText("Error loading subjects.");
            messageLabel.setVisible(true);
        }
    }

    @FXML
    private void handleSaveTask() {
        String title = titleField.getText().trim();
        String duration = durationField.getText().trim();
        String taskType = taskTypeComboBox.getValue();

        if (title.isEmpty() || duration.isEmpty() || taskType == null || deadlinePicker.getValue() == null) {
            showMessage("All fields (Title, Type, Duration, Deadline) are required.");
            return;
        }

        // Format deadline date to standard SQL string
        String deadlineDate = deadlinePicker.getValue().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));

        // Get Subject ID
        Subject selectedSubject = subjectComboBox.getSelectionModel().getSelectedItem();
        int subjectId = (selectedSubject != null) ? selectedSubject.getId() : -1;

        try {
            // Use the updated constructor and insertion method
            StudyTask newTask = new StudyTask(subjectId, title, taskType, duration, deadlineDate);

            // Insert academic task into DB
            int newId = DatabaseManager.insertAcademicTask(newTask, LoginController.currentUserID);

            if (newId != -1) {
                // Task saved successfully. Close the dialog.
                handleCancel();
            } else {
                showMessage("Failed to save task. Database error.");
            }

        } catch (SQLException e) {
            System.err.println("Database error saving new task: " + e.getMessage());
            showMessage("Database error while saving task.");
        }
    }

    @FXML
    private void handleCancel() {
        // Close the dialog window
        Stage stage = (Stage) titleField.getScene().getWindow();
        stage.close();
    }

    private void showMessage(String message) {
        messageLabel.setText(message);
        messageLabel.setVisible(true);
    }
}
