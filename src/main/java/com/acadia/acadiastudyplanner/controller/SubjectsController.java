package com.acadia.acadiastudyplanner.controller;

import com.acadia.acadiastudyplanner.data.DatabaseManager; // NEW IMPORT
import com.acadia.acadiastudyplanner.model.Subject;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;

import java.net.URL;
import java.sql.SQLException; // NEW IMPORT
import java.util.List;
import java.util.ResourceBundle;

public class SubjectsController implements Initializable {

    @FXML private ListView<Subject> subjectsListView;
    @FXML private TextField subjectNameField, difficultyField, examDateField;
    @FXML private Label formTitleLabel;
    @FXML private Button saveButton, deleteButton;

    // Use a final ObservableList for UI, but load data from DB
    private final ObservableList<Subject> subjects = FXCollections.observableArrayList();
    private Subject selectedSubject = null;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // --- PHASE 3: Load data from DB instead of hardcoded examples ---
        loadSubjectsFromDatabase();
        subjectsListView.setItems(subjects);

        subjectsListView.getSelectionModel().selectedItemProperty().addListener(
                (obs, oldV, newV) -> {
                    selectedSubject = newV;
                    if (newV != null) {
                        populateForm(newV);
                    } else {
                        clearForm();
                    }
                }
        );
        clearForm();
    }

    private void loadSubjectsFromDatabase() {
        int userId = LoginController.currentUserID;
        if (userId == -1) {
            System.err.println("Error: User not logged in. Cannot load subjects.");
            return;
        }

        try {
            List<Subject> loadedSubjects = DatabaseManager.loadSubjects(userId);
            subjects.clear();
            subjects.addAll(loadedSubjects);
        } catch (SQLException e) {
            showAlert("Database Error", "Failed to load subjects from the database: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void handleSaveSubject() {
        if (subjectNameField.getText().isBlank() || difficultyField.getText().isBlank()) {
            showAlert("Validation Error", "Subject Name and Difficulty are required.");
            return;
        }

        try {
            int difficulty = Integer.parseInt(difficultyField.getText().trim());
            String examDate = examDateField.getText().trim();
            int userId = LoginController.currentUserID;

            if (selectedSubject == null) {
                // --- PHASE 3: INSERT New Subject ---
                Subject newSubject = new Subject(subjectNameField.getText(), difficulty, examDate);
                int newId = DatabaseManager.insertSubject(newSubject, userId);

                // Update the model object with the ID from the database
                newSubject.setId(newId);
                subjects.add(newSubject);

            } else {
                // --- PHASE 3: UPDATE Existing Subject ---
                selectedSubject.setName(subjectNameField.getText());
                selectedSubject.setDifficulty(difficulty);
                selectedSubject.setExamDate(examDate);

                DatabaseManager.updateSubject(selectedSubject);
                subjectsListView.refresh();
            }
            clearForm();
        } catch (NumberFormatException e) {
            showAlert("Validation Error", "Difficulty must be a number.");
        } catch (SQLException e) {
            showAlert("Database Error", "Failed to save subject to the database: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML private void handleDeleteSubject() {
        if (selectedSubject != null) {
            try {
                // --- PHASE 3: DELETE Subject from DB ---
                DatabaseManager.deleteSubject(selectedSubject.getId());
                subjects.remove(selectedSubject);
                clearForm();
            } catch (SQLException e) {
                showAlert("Database Error", "Failed to delete subject from the database: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    @FXML private void handleClearForm() {
        subjectsListView.getSelectionModel().clearSelection();
    }

    private void populateForm(Subject subject) {
        subjectNameField.setText(subject.getName());
        difficultyField.setText(String.valueOf(subject.getDifficulty()));
        examDateField.setText(subject.getExamDate());
        formTitleLabel.setText("Edit Subject");
        saveButton.setText("Update Subject");
        deleteButton.setVisible(true);
        deleteButton.setManaged(true);
    }

    private void clearForm() {
        selectedSubject = null;
        subjectNameField.clear();
        difficultyField.clear();
        examDateField.clear();
        formTitleLabel.setText("Add a New Subject");
        saveButton.setText("Add Subject");
        deleteButton.setVisible(false);
        deleteButton.setManaged(false);
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}