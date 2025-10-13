package com.acadia.acadiastudyplanner.controller;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;

import java.net.URL;
import java.util.ResourceBundle;

public class ProgressController implements Initializable {

    @FXML
    private FlowPane progressFlowPane;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        progressFlowPane.getChildren().add(createProgressCard("Data Structures", 0.75));
        progressFlowPane.getChildren().add(createProgressCard("Calculus II", 0.40));
        progressFlowPane.getChildren().add(createProgressCard("Software Engineering", 0.90));
        progressFlowPane.getChildren().add(createProgressCard("Linear Algebra", 0.25));
    }

    private VBox createProgressCard(String subjectName, double progress) {
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
        card.getChildren().addAll(subjectLabel, progressBar, percentageLabel);
        return card;
    }
}

