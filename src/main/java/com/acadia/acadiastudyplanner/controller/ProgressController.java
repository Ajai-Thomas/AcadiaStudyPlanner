package com.acadia.acadiastudyplanner.controller;

import com.acadia.acadiastudyplanner.data.DatabaseManager;
import com.acadia.acadiastudyplanner.model.StudyTask;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;

import java.net.URL;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

public class ProgressController implements Initializable {

    @FXML private FlowPane progressFlowPane;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        loadProgressCards();
    }

    private void loadProgressCards() {
        progressFlowPane.getChildren().clear(); // Clear static data

        int userId = LoginController.currentUserID;
        if (userId == -1) {
            System.err.println("User not logged in. Cannot load progress.");
            return;
        }

        try {
            List<StudyTask> tasks = DatabaseManager.loadTasksForProgress(userId);

            // Map to hold aggregation: Subject Name -> [Total Count, Completed Count]
            Map<String, int[]> progressData = new HashMap<>();

            // --- Step 1: Aggregate Data ---
            for (StudyTask task : tasks) {
                String subjectName = task.getSubjectName();
                progressData.putIfAbsent(subjectName, new int[]{0, 0}); // [Total, Completed]

                int[] counts = progressData.get(subjectName);
                counts[0]++; // Increment total task count

                if (task.getStatus().equalsIgnoreCase("Completed")) {
                    counts[1]++; // Increment completed task count
                }
            }

            // --- Step 2: Generate UI Cards ---
            if (progressData.isEmpty()) {
                progressFlowPane.getChildren().add(new Label("No subjects or tasks defined yet."));
            } else {
                for (Map.Entry<String, int[]> entry : progressData.entrySet()) {
                    String subject = entry.getKey();
                    int total = entry.getValue()[0];
                    int completed = entry.getValue()[1];

                    double progress = (total > 0) ? (double) completed / total : 0.0;

                    progressFlowPane.getChildren().add(createProgressCard(subject, progress, completed, total));
                }
            }

        } catch (SQLException e) {
            System.err.println("Error loading progress data: " + e.getMessage());
            progressFlowPane.getChildren().add(new Label("Failed to load progress due to a database error."));
        }
    }

    private VBox createProgressCard(String subjectName, double progress, int completed, int total) {
        VBox card = new VBox(10);
        card.getStyleClass().add("card");
        card.setPadding(new Insets(20));
        card.setPrefWidth(250);

        Label subjectLabel = new Label(subjectName);
        subjectLabel.getStyleClass().add("card-title");
        ProgressBar progressBar = new ProgressBar(progress);
        progressBar.setMaxWidth(Double.MAX_VALUE);
        progressBar.getStyleClass().add("progress-bar-custom");

        Label percentageLabel = new Label(String.format("%.0f%% Complete", progress * 100));
        percentageLabel.getStyleClass().add("form-label");

        Label detailLabel = new Label(String.format("(%d out of %d tasks done)", completed, total));
        detailLabel.getStyleClass().add("item-time");

        card.getChildren().addAll(subjectLabel, progressBar, percentageLabel, detailLabel);
        return card;
    }
}
