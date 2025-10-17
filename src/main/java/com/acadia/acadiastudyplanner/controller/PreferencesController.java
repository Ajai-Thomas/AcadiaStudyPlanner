package com.acadia.acadiastudyplanner.controller;

import com.acadia.acadiastudyplanner.data.DatabaseManager;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.net.URL;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.ResourceBundle;

public class PreferencesController implements Initializable {

    @FXML private TextArea prefsTextArea;
    @FXML private TextField breakLengthField;
    @FXML private TextField breakFrequencyField;
    @FXML private ComboBox<String> dayComboBox;
    @FXML private TextField startHourField;
    @FXML private TextField endHourField;
    @FXML private ListView<String> availabilityListView;

    private final ObservableList<String> availableSlots = FXCollections.observableArrayList();
    private final Map<String, Integer> slotIdMap = new HashMap<>(); // Holds DB IDs for existing slots

    // Days array for ComboBox initialization
    private static final List<String> DAYS = Arrays.asList("MON", "TUE", "WED", "THU", "FRI", "SAT", "SUN");

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        dayComboBox.setItems(FXCollections.observableArrayList(DAYS));
        availabilityListView.setItems(availableSlots);

        // --- NEW: Custom Cell Factory for Visual Consistency ---
        availabilityListView.setCellFactory(lv -> new ListCell<String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                    setStyle(null); // Clear style when empty
                } else {
                    setText(item);
                    // Highlight weekends
                    if (item.startsWith("SAT") || item.startsWith("SUN")) {
                        setStyle("-fx-background-color: #3f3f3f; -fx-text-fill: #ffa000;"); // Yellow text on dark background
                    } else {
                        setStyle("-fx-background-color: transparent; -fx-text-fill: white;");
                    }
                }
            }
        });
        // --------------------------------------------------------

        loadUserPreferencesAndSlots();
        setupSlotDeletion();
    }

    private void setupSlotDeletion() {
        availabilityListView.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {
                String selectedSlot = availabilityListView.getSelectionModel().getSelectedItem();
                if (selectedSlot != null) {
                    confirmAndDeleteSlot(selectedSlot);
                }
            }
        });
    }

    private void confirmAndDeleteSlot(String slotString) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirm Deletion");
        alert.setHeaderText("Delete Available Slot?");
        alert.setContentText("Are you sure you want to delete this time slot: " + slotString + "?");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            availableSlots.remove(slotString);
            slotIdMap.remove(slotString);
        }
    }

    private void loadUserPreferencesAndSlots() {
        int userId = LoginController.currentUserID;
        if (userId == -1) return;

        try {
            Map<String, Object> prefs = DatabaseManager.loadUserPreferences(userId);
            if (!prefs.isEmpty()) {
                breakLengthField.setText(String.valueOf(prefs.get("BreakLength")));
                breakFrequencyField.setText(String.valueOf(prefs.get("BreakFrequency")));
                prefsTextArea.setText((String)prefs.get("LearningPreferences"));
            }

            List<Map<String, Object>> slots = DatabaseManager.loadAvailableSlots(userId);
            availableSlots.clear();
            slotIdMap.clear();
            for (Map<String, Object> slot : slots) {
                String slotString = formatSlotString(
                        (String)slot.get("DayOfWeek"),
                        (String)slot.get("StartTime"),
                        (String)slot.get("EndTime")
                );
                availableSlots.add(slotString);
                slotIdMap.put(slotString, (Integer)slot.get("SlotID"));
            }
        } catch (SQLException e) {
            showError("Load Error", "Failed to load preferences and slots: " + e.getMessage());
        }
    }

    @FXML
    private void handleAddSlot() {
        String day = dayComboBox.getSelectionModel().getSelectedItem();
        String start = startHourField.getText().trim();
        String end = endHourField.getText().trim();

        if (day == null || start.isEmpty() || end.isEmpty()) {
            showError("Input Error", "Please select a day and enter valid start/end times (HH:MM).");
            return;
        }

        if (!start.matches("\\d{2}:\\d{2}") || !end.matches("\\d{2}:\\d{2}")) {
            showError("Input Error", "Time must be in HH:MM format (e.g., 09:00).");
            return;
        }

        String slotString = formatSlotString(day, start, end);
        availableSlots.add(slotString);

        dayComboBox.getSelectionModel().clearSelection();
        startHourField.clear();
        endHourField.clear();
    }

    @FXML
    private void handleSaveAllPreferences() {
        int userId = LoginController.currentUserID;
        if (userId == -1) return;

        try {
            int breakLength = parseNumericField(breakLengthField, "Break Length");
            int breakFrequency = parseNumericField(breakFrequencyField, "Break Frequency");
            String prefsText = prefsTextArea.getText();

            DatabaseManager.saveUserPreferences(userId, breakLength, breakFrequency, prefsText);

            DatabaseManager.deleteAllAvailableSlots(userId);
            for (String slotString : availableSlots) {
                String[] parts = slotString.split(" "); // "DAY HH:MM - HH:MM"
                DatabaseManager.addAvailableSlot(userId, parts[0], parts[1], parts[3]);
            }

            showInfo("Success", "All preferences and availability slots have been saved!");
            handleCancel();

        } catch (NumberFormatException e) {
        } catch (SQLException e) {
            showError("Database Error", "Failed to save data: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void handleCancel() {
        Stage stage = (Stage) prefsTextArea.getScene().getWindow();
        stage.close();
    }

    private String formatSlotString(String day, String start, String end) {
        return String.format("%s %s - %s", day, start, end);
    }

    private int parseNumericField(TextField field, String fieldName) throws NumberFormatException {
        try {
            return Integer.parseInt(field.getText().trim());
        } catch (NumberFormatException e) {
            showError("Input Error", fieldName + " must be a whole number.");
            throw e;
        }
    }

    private void showError(String title, String content) {
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
