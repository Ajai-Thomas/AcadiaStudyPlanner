package com.acadia.acadiastudyplanner.service;

import com.acadia.acadiastudyplanner.data.DatabaseManager;
import com.acadia.acadiastudyplanner.model.Subject;
import com.acadia.acadiastudyplanner.model.StudyTask;
import com.acadia.acadiastudyplanner.controller.LoginController;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class SchedulingService {

    // IMPORTANT: Load the API key from an environment variable for security
    private static final String API_KEY = System.getenv("GEMINI_API_KEY");
    private static final String API_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash:generateContent?key=" + API_KEY;
    private static final HttpClient HTTP_CLIENT = HttpClient.newHttpClient();

    private final int userId;

    public SchedulingService() {
        this.userId = LoginController.currentUserID;
        if (this.userId == -1) {
            throw new IllegalStateException("SchedulingService initialized without a logged-in user.");
        }
        if (API_KEY == null || API_KEY.isBlank()) {
            throw new IllegalStateException("GEMINI_API_KEY environment variable not set.");
        }
    }

    public String generateWeeklySchedule() throws SQLException, IOException, InterruptedException {
        List<Subject> subjects = DatabaseManager.loadSubjects(userId);
        List<StudyTask> pendingTasks = getPendingTasks();
        Map<String, Object> prefs = DatabaseManager.loadUserPreferences(userId);
        List<Map<String, Object>> availability = DatabaseManager.loadAvailableSlots(userId);

        String systemInstruction = buildSystemInstruction(prefs);
        String userQuery = buildUserQuery(subjects, pendingTasks, availability);
        String jsonPayload = buildJsonPayload(userQuery, systemInstruction);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(API_URL))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonPayload))
                .build();

        HttpResponse<String> response = HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            throw new IOException("Gemini API call failed with status: " + response.statusCode() + ". Response body: " + response.body());
        }
        return response.body();
    }

    private List<StudyTask> getPendingTasks() throws SQLException {
        return DatabaseManager.loadTasksForProgress(userId).stream()
                .filter(task -> task.getStatus().equalsIgnoreCase("Pending"))
                .collect(Collectors.toList());
    }

    private String buildSystemInstruction(Map<String, Object> prefs) {
        String preferences = (String) prefs.getOrDefault("LearningPreferences", "No specific learning style provided.");
        int breakLength = (int) prefs.getOrDefault("BreakLength", 15);
        int breakFrequency = (int) prefs.getOrDefault("BreakFrequency", 60);

        return String.format(
                "You are an expert academic scheduling AI. Your goal is to create an optimized weekly study schedule. " +
                        "Prioritize tasks based on difficulty, duration, and deadline. " +
                        "The user's preference is: %s. " +
                        "Ensure study blocks are separated by a minimum %d-minute break, occurring after every %d minutes of focused study. " +
                        "The output MUST be a JSON array of scheduled tasks for the week.",
                preferences, breakLength, breakFrequency
        );
    }

    private String buildUserQuery(List<Subject> subjects, List<StudyTask> tasks, List<Map<String, Object>> availability) {
        String subjectsStr = subjects.stream()
                .map(s -> String.format("{Name: %s, Difficulty: %d, Exam: %s}", s.getName(), s.getDifficulty(), s.getExamDate()))
                .collect(Collectors.joining("; "));

        String tasksStr = tasks.stream()
                .map(t -> String.format("{Title: %s, Subject: %s, Type: %s, Duration: %s, Deadline: %s}",
                        t.getTitle(), t.getSubjectName(), t.getTaskType(), t.getDurationEstimate(), t.getDeadlineDate()))
                .collect(Collectors.joining("; "));

        String availabilityStr = availability.stream()
                .map(a -> String.format("{Day: %s, Start: %s, End: %s}", a.get("DayOfWeek"), a.get("StartTime"), a.get("EndTime")))
                .collect(Collectors.joining("; "));

        return String.format(
                "--- USER DATA ---\n" +
                        "Subjects: [%s]\n" +
                        "Pending Tasks: [%s]\n" +
                        "Available Slots (REQUIRED for scheduling): [%s]\n\n" +
                        "Generate the schedule for the next 7 days, starting from Monday, using ONLY the available slots. " +
                        "Each scheduled block must be under 120 minutes. Output the schedule as a JSON array.",
                subjectsStr, tasksStr, availabilityStr
        );
    }

    private String buildJsonPayload(String userQuery, String systemInstruction) {
        String responseSchema = """
            {
              "type": "ARRAY",
              "items": {
                "type": "OBJECT",
                "properties": {
                  "day": {"type": "STRING", "description": "MON, TUE, WED, etc."},
                  "subject_name": {"type": "STRING"},
                  "task_title": {"type": "STRING"},
                  "time_start": {"type": "STRING", "description": "HH:MM format"},
                  "time_end": {"type": "STRING", "description": "HH:MM format"}
                },
                "required": ["day", "subject_name", "task_title", "time_start", "time_end"]
              }
            }
            """;
        return String.format(
                "{\n" +
                        "  \"contents\": [{\"parts\": [{\"text\": \"%s\"}]}],\n" +
                        "  \"systemInstruction\": {\"parts\": [{\"text\": \"%s\"}]},\n" +
                        "  \"generationConfig\": {\n" +
                        "    \"responseMimeType\": \"application/json\",\n" +
                        "    \"responseSchema\": %s\n" +
                        "  }\n" +
                        "}",
                userQuery.replace("\"", "\\\"").replace("\n", "\\n"),
                systemInstruction.replace("\"", "\\\"").replace("\n", "\\n"),
                responseSchema.replaceAll("\\s+", " ")
        );
    }
}