package com.acadia.acadiastudyplanner.model;

public class StudyTask {
    private int id;
    private int subjectId;
    private String title;
    private String taskType;
    private String durationEstimate;
    private String deadlineDate;
    private String startTime;         // Re-added for scheduled tasks
    private String endTime;           // Re-added for scheduled tasks
    private String dayOfWeek;         // Re-added for scheduled tasks
    private String status;
    private String subjectName;       // Temporary field for display purposes

    // --- Constructor for loading from DB (Must match loadTasksForProgress in DatabaseManager) ---
    public StudyTask(int id, int subjectId, String title, String taskType, String durationEstimate,
                     String deadlineDate, String startTime, String endTime, String dayOfWeek, String status) {
        this.id = id;
        this.subjectId = subjectId;
        this.title = title;
        this.taskType = taskType;
        this.durationEstimate = durationEstimate;
        this.deadlineDate = deadlineDate;
        this.startTime = startTime;
        this.endTime = endTime;
        this.dayOfWeek = dayOfWeek;
        this.status = status;
        this.subjectName = (subjectId == -1) ? "General" : null;
    }

    // --- Constructor for creating a NEW PENDING task (Input fields) ---
    public StudyTask(int subjectId, String title, String taskType, String durationEstimate, String deadlineDate) {
        this.id = -1;
        this.subjectId = subjectId;
        this.title = title;
        this.taskType = taskType;
        this.durationEstimate = durationEstimate;
        this.deadlineDate = deadlineDate;
        this.status = "Pending";
        this.subjectName = "General";
    }

    // Getters
    public int getId() { return id; }
    public int getSubjectId() { return subjectId; }
    public String getTitle() { return title; }
    public String getTaskType() { return taskType; }
    public String getDurationEstimate() { return durationEstimate; }
    public String getDeadlineDate() { return deadlineDate; }
    public String getStartTime() { return startTime; }
    public String getEndTime() { return endTime; }
    public String getDayOfWeek() { return dayOfWeek; }
    public String getStatus() { return status; }
    public String getSubjectName() { return subjectName; }

    // Setters
    public void setId(int id) { this.id = id; }
    public void setStatus(String status) { this.status = status; }
    public void setSubjectName(String subjectName) { this.subjectName = subjectName; }

    @Override
    public String toString() {
        return String.format("[%s] %s: %s (Due: %s, Time: %s)", taskType, title, subjectId == -1 ? "General" : "SubjectID " + subjectId, deadlineDate, durationEstimate);
    }
}
