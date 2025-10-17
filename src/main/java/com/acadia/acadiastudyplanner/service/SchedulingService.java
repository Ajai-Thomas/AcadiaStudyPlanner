//AIzaSyD4eg56lhj2dxqb1Vey4ipmF_aXd72aBYA

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

// NOTE: You will need a JSON library for parsing (like Gson or Jackson).
// For simplicity in this example, we will use basic string concatenation for JSON payload creation.

public class SchedulingService {

    // IMPORTANT: Replace this with your actual Gemini API Key
    private static final String API_KEY = "AIzaSyD4eg56lhj2dxqb1Vey4ipmF_aXd72aBYA";
    private static final String API_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent?key=" + API_KEY;
    private static final HttpClient HTTP_CLIENT = HttpClient.newHttpClient();

    private final int userId;

    public SchedulingService() {
        this.userId = LoginController.currentUserID;
        if (this.userId == -1) {
            throw new IllegalStateException("SchedulingService initialized without a logged-in user.");
        }
    }

    /**
     * Executes the scheduling logic by gathering data and calling the Gemini API.
     * @return The raw JSON schedule response from the API.
     */
    public String generateWeeklySchedule() throws SQLException, IOException, InterruptedException {
        // 1. Gather all required data
        List<Subject> subjects = DatabaseManager.loadSubjects(userId);
        List<StudyTask> pendingTasks = getPendingTasks(); // Assuming this method is implemented
        Map<String, Object> prefs = DatabaseManager.loadUserPreferences(userId);
        List<Map<String, Object>> availability = DatabaseManager.loadAvailableSlots(userId);

        // 2. Build the LLM prompt and request payload
        String systemInstruction = buildSystemInstruction(prefs);
        String userQuery = buildUserQuery(subjects, pendingTasks, availability);
        String jsonPayload = buildJsonPayload(userQuery, systemInstruction);

        // 3. Execute API Call
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(API_URL))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonPayload))
                .build();

        HttpResponse<String> response = HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            throw new IOException("Gemini API call failed with status: " + response.statusCode() + ". Response body: " + response.body());
        }

        // TODO: In the next step, we would parse this response.body string into a schedule object.
        return response.body();
    }

    // --- Data Gathering Helpers ---

    private List<StudyTask> getPendingTasks() throws SQLException {
        // Loads all tasks and filters for pending status
        return DatabaseManager.loadTasksForProgress(userId).stream()
                .filter(task -> task.getStatus().equalsIgnoreCase("Pending"))
                .collect(Collectors.toList());
    }

    // --- Prompt Construction Methods (CRITICAL) ---

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

    // Simplistic JSON payload builder for text generation
    private String buildJsonPayload(String userQuery, String systemInstruction) {
        // Define the structured JSON output schema expected from the LLM
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

        // FIX: The 'config' field must be nested inside 'generationConfig'
        return String.format(
                "{\n" +
                        "  \"contents\": [{\"parts\": [{\"text\": \"%s\"}]}],\n" +
                        "  \"systemInstruction\": {\"parts\": [{\"text\": \"%s\"}]},\n" +
                        "  \"generationConfig\": {\n" +
                        "    \"responseMimeType\": \"application/json\",\n" +
                        "    \"responseSchema\": %s\n" +
                        "  }\n" +
                        "}",
                userQuery.replace("\"", "\\\"").replace("\n", "\\n"), // Escape user query
                systemInstruction.replace("\"", "\\\"").replace("\n", "\\n"), // Escape system instruction
                responseSchema.replaceAll("\\s+", " ") // Compact the schema
        );
    }
}
