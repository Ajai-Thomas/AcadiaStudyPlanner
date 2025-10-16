package com.acadia.acadiastudyplanner.controller;

import com.acadia.acadiastudyplanner.model.Subject;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;

import java.net.URL;
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
        subjects.add(new Subject("Data Structures", 4, "2025-12-10"));
        subjects.add(new Subject("Calculus II", 5, "2025-12-18"));
        subjects.add(new Subject("Software Engineering", 3, ""));
        subjectsListView.setItems(subjects);
        subjectsListView.getSelectionModel().selectedItemProperty().addListener(
                (obs, oldV, newV) -> {
                    selectedSubject = newV;
                    if (newV != null) populateForm(newV); else clearForm();
                }
        );
        clearForm();
    }

    @FXML
    private void handleSaveSubject() {
        if (subjectNameField.getText().isBlank() || difficultyField.getText().isBlank()) {
            System.err.println("Subject Name and Difficulty are required."); return;
        }
        try {
            int difficulty = Integer.parseInt(difficultyField.getText().trim());
            String examDate = examDateField.getText().trim();
            if (selectedSubject == null) {
                subjects.add(new Subject(subjectNameField.getText(), difficulty, examDate));
            } else {
                selectedSubject.setName(subjectNameField.getText());
                selectedSubject.setDifficulty(difficulty);
                selectedSubject.setExamDate(examDate);
                subjectsListView.refresh();
            }
            clearForm();
        } catch (NumberFormatException e) {
            System.err.println("Difficulty must be a number.");
        }
    }

    @FXML private void handleDeleteSubject() {
        if (selectedSubject != null) subjects.remove(selectedSubject);
        // FIX 1: Clear the form after deleting the subject.
        clearForm();
    }

    @FXML private void handleClearForm() {
        subjectsListView.getSelectionModel().clearSelection();
        // FIX 2: Explicitly call clearForm() to ensure the form fields are reset.
        clearForm();
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
}