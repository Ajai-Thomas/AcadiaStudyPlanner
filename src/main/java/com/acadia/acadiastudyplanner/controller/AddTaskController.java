package com.acadia.acadiastudyplanner.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import java.time.LocalDate;

public class AddTaskController {

    @FXML private TextField taskNameField;
    @FXML private TextField subjectField;
    @FXML private DatePicker datePicker;
    @FXML private TextField durationField;

    // FIX 1: Field to hold the task data after a successful save.
    private TaskData newTaskData = null;

    // FIX 2: Simple inner class to hold the task data for transfer
    public static class TaskData {
        public final String name;
        public final String subject;
        public final String date;
        public final String duration;

        public TaskData(String name, String subject, String date, String duration) {
            this.name = name;
            this.subject = subject;
            this.date = date;
            this.duration = duration;
        }
    }

    @FXML
    private void handleAddTask() {
        String name = taskNameField.getText().trim();
        String subject = subjectField.getText().trim();
        LocalDate dateValue = datePicker.getValue();
        String date = (dateValue != null) ? dateValue.toString() : "";
        String duration = durationField.getText().trim();

        if (name.isEmpty() || subject.isEmpty() || date.isEmpty()) {
            showError("Please fill in Task Name, Subject, and Date.");
            return;
        }

        // FIX 3: Store the new task data before closing
        newTaskData = new TaskData(name, subject, date, duration);

        System.out.println("New Task Added: " + name + ", Subject: " + subject + ", Date: " + date + ", Duration: " + duration);
        // Close dialog window
        Stage stage = (Stage) taskNameField.getScene().getWindow();
        stage.close();
    }

    // FIX 4: Public getter for the DashboardController to retrieve the data
    public TaskData getNewTaskData() {
        return newTaskData;
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Input Error");
        alert.setContentText(message);
        alert.showAndWait();
    }
}