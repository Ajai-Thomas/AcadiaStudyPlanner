package com.acadia.acadiastudyplanner.data;

import com.acadia.acadiastudyplanner.model.Subject;
import com.acadia.acadiastudyplanner.model.StudyTask;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

/**
 * Handles all database connectivity and persistent data operations using SQLite.
 */
public class DatabaseManager {

    private static final String DB_URL = "jdbc:sqlite:acadia_study_planner.db";

    public static Connection connect() throws SQLException {
        try {
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException e) {
            System.err.println("Error: SQLite JDBC driver not found.");
            throw new SQLException("SQLite JDBC driver not found.", e);
        }
        return DriverManager.getConnection(DB_URL);
    }

    public static void initializeDatabase() {
        try (Connection conn = connect();
             Statement stmt = conn.createStatement()) {

            // 1. User Table (All names are CamelCase, matching the project convention)
            String createUsersTable = "CREATE TABLE IF NOT EXISTS User (" +
                    "UserID INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "Username TEXT NOT NULL UNIQUE," +
                    "PasswordHash TEXT NOT NULL," +
                    "DisplayName TEXT," +
                    "BreakLength INTEGER DEFAULT 15," +
                    "BreakFrequency INTEGER DEFAULT 60," +
                    "LearningPreferences TEXT" +
                    ");";
            stmt.execute(createUsersTable);

            // 2. Subject Table
            String createSubjectsTable = "CREATE TABLE IF NOT EXISTS Subject (" +
                    "SubjectID INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "UserID INTEGER NOT NULL," +
                    "Name TEXT NOT NULL," +
                    "Difficulty INTEGER NOT NULL," +
                    "ExamDate TEXT," +
                    "FOREIGN KEY (UserID) REFERENCES User(UserID) ON DELETE CASCADE" +
                    ");";
            stmt.execute(createSubjectsTable);

            // 3. StudyTask Table (FINAL SCHEMA)
            String createTasksTable = "CREATE TABLE IF NOT EXISTS StudyTask (" +
                    "TaskID INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "UserID INTEGER NOT NULL," +
                    "SubjectID INTEGER," +
                    "Title TEXT NOT NULL," +
                    "TaskType TEXT," +
                    "DurationEstimate TEXT," +
                    "DeadlineDate TEXT," +
                    "StartTime TEXT," +
                    "EndTime TEXT," +
                    "DayOfWeek TEXT," +
                    "Status TEXT NOT NULL DEFAULT 'Pending'," +
                    "FOREIGN KEY (UserID) REFERENCES User(UserID) ON DELETE CASCADE," +
                    "FOREIGN KEY (SubjectID) REFERENCES Subject(SubjectID) ON DELETE SET NULL" +
                    ");";
            stmt.execute(createTasksTable);

            // 4. Availability Table
            String createAvailabilityTable = "CREATE TABLE IF NOT EXISTS Availability (" +
                    "SlotID INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "UserID INTEGER NOT NULL," +
                    "DayOfWeek TEXT NOT NULL," +
                    "StartTime TEXT NOT NULL," +
                    "EndTime TEXT NOT NULL," +
                    "FOREIGN KEY (UserID) REFERENCES User(UserID) ON DELETE CASCADE" +
                    ");";
            stmt.execute(createAvailabilityTable);

            System.out.println("Database initialization complete.");

        } catch (SQLException e) {
            System.err.println("Database initialization failed: " + e.getMessage());
        }
    }

    // --- User Persistence Methods (Standardized to use proper casing) ---
    public static boolean userExists(String username) throws SQLException {
        String sql = "SELECT COUNT(*) FROM User WHERE Username = ?";
        try (Connection conn = connect(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username);
            ResultSet rs = pstmt.executeQuery();
            return rs.getInt(1) > 0;
        }
    }

    public static void insertUser(String username, String password, String displayName) throws SQLException {
        String sql = "INSERT INTO User(Username, PasswordHash, DisplayName) VALUES(?, ?, ?)";
        try (Connection conn = connect(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username);
            pstmt.setString(2, password);
            pstmt.setString(3, displayName);
            pstmt.executeUpdate();
        }
    }

    public static int validateAndGetUserID(String username, String password) throws SQLException {
        String sql = "SELECT UserID, PasswordHash FROM User WHERE Username = ?";
        try (Connection conn = connect(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                String storedPassword = rs.getString("PasswordHash");
                if (storedPassword.equals(password)) {
                    return rs.getInt("UserID");
                }
            }
            return -1;
        }
    }

    public static String getDisplayName(int userId) throws SQLException {
        String sql = "SELECT DisplayName FROM User WHERE UserID = ?";
        try (Connection conn = connect(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                String displayName = rs.getString("DisplayName");
                return (displayName == null || displayName.trim().isEmpty()) ? "User" : displayName;
            }
            return "User";
        }
    }

    // --- Subject Persistence Methods (Standardized) ---
    public static int insertSubject(Subject subject, int userId) throws SQLException {
        String sql = "INSERT INTO Subject (UserID, Name, Difficulty, ExamDate) VALUES (?, ?, ?, ?)";
        try (Connection conn = connect(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            conn.setAutoCommit(false); pstmt.setInt(1, userId); pstmt.setString(2, subject.getName());
            pstmt.setInt(3, subject.getDifficulty()); pstmt.setString(4, subject.getExamDate());
            int affectedRows = pstmt.executeUpdate();
            if (affectedRows == 0) { conn.rollback(); throw new SQLException("Creating subject failed, no rows affected."); }
            int newId = -1;
            try (Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery("SELECT last_insert_rowid()")) {
                if (rs.next()) newId = rs.getInt(1);
                else { conn.rollback(); throw new SQLException("Creating subject failed, no ID obtained."); }
            }
            conn.commit(); conn.setAutoCommit(true);
            return newId;
        }
    }

    public static void updateSubject(Subject subject) throws SQLException {
        String sql = "UPDATE Subject SET Name = ?, Difficulty = ?, ExamDate = ? WHERE SubjectID = ?";
        try (Connection conn = connect(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, subject.getName()); pstmt.setInt(2, subject.getDifficulty());
            pstmt.setString(3, subject.getExamDate()); pstmt.setInt(4, subject.getId());
            pstmt.executeUpdate();
        }
    }

    public static void deleteSubject(int subjectId) throws SQLException {
        String sql = "DELETE FROM Subject WHERE SubjectID = ?";
        try (Connection conn = connect(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, subjectId);
            pstmt.executeUpdate();
        }
    }

    public static List<Subject> loadSubjects(int userId) throws SQLException {
        List<Subject> subjects = new ArrayList<>();
        String sql = "SELECT SubjectID, Name, Difficulty, ExamDate FROM Subject WHERE UserID = ?";
        try (Connection conn = connect(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Subject subject = new Subject(rs.getInt("SubjectID"), rs.getString("Name"), rs.getInt("Difficulty"), rs.getString("ExamDate"));
                    subjects.add(subject);
                }
            }
        }
        return subjects;
    }

    // --- User Preferences & Availability Methods (Standardized) ---
    public static void saveUserPreferences(int userId, int breakLength, int breakFrequency, String preferences) throws SQLException {
        String sql = "UPDATE User SET BreakLength = ?, BreakFrequency = ?, LearningPreferences = ? WHERE UserID = ?";
        try (Connection conn = connect(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, breakLength); pstmt.setInt(2, breakFrequency); pstmt.setString(3, preferences); pstmt.setInt(4, userId);
            pstmt.executeUpdate();
        }
    }

    public static Map<String, Object> loadUserPreferences(int userId) throws SQLException {
        Map<String, Object> prefs = new HashMap<>();
        String sql = "SELECT BreakLength, BreakFrequency, LearningPreferences, DisplayName FROM User WHERE UserID = ?";
        try (Connection conn = connect(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    prefs.put("BreakLength", rs.getInt("BreakLength"));
                    prefs.put("BreakFrequency", rs.getInt("BreakFrequency"));
                    prefs.put("LearningPreferences", rs.getString("LearningPreferences"));
                    prefs.put("DisplayName", rs.getString("DisplayName"));
                }
            }
        }
        return prefs;
    }

    public static List<Map<String, Object>> loadAvailableSlots(int userId) throws SQLException {
        List<Map<String, Object>> slots = new ArrayList<>();
        String sql = "SELECT SlotID, DayOfWeek, StartTime, EndTime FROM Availability WHERE UserID = ?";
        try (Connection conn = connect(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> slot = new HashMap<>();
                    slot.put("SlotID", rs.getInt("SlotID"));
                    slot.put("DayOfWeek", rs.getString("DayOfWeek"));
                    slot.put("StartTime", rs.getString("StartTime"));
                    slot.put("EndTime", rs.getString("EndTime"));
                    slots.add(slot);
                }
            }
        }
        return slots;
    }

    public static int addAvailableSlot(int userId, String dayOfWeek, String startTime, String endTime) throws SQLException {
        String sql = "INSERT INTO Availability (UserID, DayOfWeek, StartTime, EndTime) VALUES (?, ?, ?, ?)";
        try (Connection conn = connect(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, userId); pstmt.setString(2, dayOfWeek); pstmt.setString(3, startTime); pstmt.setString(4, endTime);
            pstmt.executeUpdate();
            try (Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery("SELECT last_insert_rowid()")) {
                return rs.next() ? rs.getInt(1) : -1;
            }
        }
    }

    public static void deleteAvailableSlot(int slotId) throws SQLException {
        String sql = "DELETE FROM Availability WHERE SlotID = ?";
        try (Connection conn = connect(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, slotId);
            pstmt.executeUpdate();
        }
    }

    public static void deleteAllAvailableSlots(int userId) throws SQLException {
        String sql = "DELETE FROM Availability WHERE UserID = ?";
        try (Connection conn = connect(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            pstmt.executeUpdate();
        }
    }

    public static void updateDisplayName(int userId, String newName) throws SQLException {
        String sql = "UPDATE User SET DisplayName = ? WHERE UserID = ?";
        try (Connection conn = connect(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, newName);
            pstmt.setInt(2, userId);
            pstmt.executeUpdate();
        }
    }

    // --- Task Persistence Methods (Corrected to load all 10 fields) ---

    public static int insertAcademicTask(StudyTask task, int userId) throws SQLException {
        String sql = "INSERT INTO StudyTask (UserID, SubjectID, Title, TaskType, DurationEstimate, DeadlineDate, Status) VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            conn.setAutoCommit(false);

            pstmt.setInt(1, userId);
            if (task.getSubjectId() == -1) { pstmt.setNull(2, java.sql.Types.INTEGER); } else { pstmt.setInt(2, task.getSubjectId()); }
            pstmt.setString(3, task.getTitle());
            pstmt.setString(4, task.getTaskType());
            pstmt.setString(5, task.getDurationEstimate());
            pstmt.setString(6, task.getDeadlineDate());
            pstmt.setString(7, task.getStatus());

            int affectedRows = pstmt.executeUpdate();

            if (affectedRows == 0) { conn.rollback(); throw new SQLException("Creating task failed, no rows affected."); }

            int newId = -1;
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery("SELECT last_insert_rowid()")) {
                if (rs.next()) { newId = rs.getInt(1); }
                else { conn.rollback(); throw new SQLException("Creating task failed, no ID obtained."); }
            }

            conn.commit();
            conn.setAutoCommit(true);
            return newId;
        }
    }

    /**
     * Loads all tasks for a user, ensuring all 10 fields are loaded for the StudyTask constructor.
     */
    public static List<StudyTask> loadTasksForProgress(int userId) throws SQLException {
        List<StudyTask> tasks = new ArrayList<>();
        // SELECT T.* ensures all columns (TaskID, UserID, Title, TaskType, DurationEstimate, DeadlineDate, StartTime, EndTime, DayOfWeek, Status) are selected
        String sql = "SELECT T.*, S.Name AS SubjectName " +
                "FROM StudyTask T LEFT JOIN Subject S ON T.SubjectID = S.SubjectID " +
                "WHERE T.UserID = ?";

        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, userId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    StudyTask task = new StudyTask(
                            rs.getInt("TaskID"),
                            rs.getInt("SubjectID"),
                            rs.getString("Title"),
                            rs.getString("TaskType"),
                            rs.getString("DurationEstimate"),
                            rs.getString("DeadlineDate"),
                            rs.getString("StartTime"),
                            rs.getString("EndTime"),
                            rs.getString("DayOfWeek"),
                            rs.getString("Status")
                    );
                    String subjectName = rs.getString("SubjectName");
                    if (subjectName != null) {
                        task.setSubjectName(subjectName);
                    }
                    tasks.add(task);
                }
            }
        }
        return tasks;
    }

    public static void updateTaskStatus(int taskId, String newStatus) throws SQLException {
        String sql = "UPDATE StudyTask SET Status = ? WHERE TaskID = ?";
        try (Connection conn = connect(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, newStatus); pstmt.setInt(2, taskId); pstmt.executeUpdate();
        }
    }

    public static void saveScheduledTask(int userId, int subjectId, String title, String day, String start, String end) throws SQLException {
        String sql = "INSERT INTO StudyTask (UserID, SubjectID, Title, StartTime, EndTime, DayOfWeek, Status) VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, userId);
            if (subjectId == -1) { pstmt.setNull(2, java.sql.Types.INTEGER); } else { pstmt.setInt(2, subjectId); }
            pstmt.setString(3, title);
            pstmt.setString(4, start);
            pstmt.setString(5, end);
            pstmt.setString(6, day);
            pstmt.setString(7, "Scheduled");

            pstmt.executeUpdate();
        }
    }

    public static void clearScheduledTasks(int userId) throws SQLException {
        String sql = "DELETE FROM StudyTask WHERE UserID = ? AND Status = 'Scheduled'";
        try (Connection conn = connect(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            pstmt.executeUpdate();
        }
    }
}