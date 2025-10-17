package com.acadia.acadiastudyplanner.controller;

import com.acadia.acadiastudyplanner.data.DatabaseManager;
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
import java.sql.SQLException;
import java.util.List;
import java.util.ResourceBundle;

public class SubjectsController implements Initializable {

    @FXML private ListView<Subject> subjectsListView;
    @FXML private TextField subjectNameField, difficultyField, examDateField;
    @FXML private Label formTitleLabel;
    @FXML private Button saveButton, deleteButton;

    private final ObservableList<Subject> subjects = FXCollections.observableArrayList();
    private Subject selectedSubject = null;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        loadSubjectsFromDatabase();
        subjectsListView.setItems(subjects);
        subjectsListView.getSelectionModel().selectedItemProperty().addListener(
                (obs, oldV, newV) -> {
                    selectedSubject = newV;
                    if (newV != null) populateForm(newV); else clearForm();
                }
        );
        clearForm();
    }

    private void loadSubjectsFromDatabase() {
        try {
            List<Subject> loadedSubjects = DatabaseManager.loadSubjects(LoginController.currentUserID);
            subjects.setAll(loadedSubjects);
        } catch (SQLException e) {
            showAlert("Database Error", "Failed to load subjects: " + e.getMessage());
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
            if (selectedSubject == null) {
                Subject newSubject = new Subject(subjectNameField.getText(), difficulty, examDate);
                int newId = DatabaseManager.insertSubject(newSubject, LoginController.currentUserID);
                newSubject.setId(newId);
                subjects.add(newSubject);
            } else {
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
            showAlert("Database Error", "Failed to save subject: " + e.getMessage());
        }
    }

    @FXML private void handleDeleteSubject() {
        if (selectedSubject != null) {
            try {
                DatabaseManager.deleteSubject(selectedSubject.getId());
                subjects.remove(selectedSubject);
                clearForm();
            } catch (SQLException e) {
                showAlert("Database Error", "Failed to delete subject: " + e.getMessage());
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