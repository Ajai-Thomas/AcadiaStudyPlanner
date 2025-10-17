package com.acadia.acadiastudyplanner.model;

public class Subject {
    // Phase 2: NEW FIELD - This ID links the model object to the database record.
    private int id;
    private String name;
    private int difficulty;
    private String examDate;

    // Phase 2: NEW CONSTRUCTOR - Used when loading subjects FROM the database (includes ID)
    public Subject(int id, String name, int difficulty, String examDate) {
        this.id = id;
        this.name = name;
        this.difficulty = difficulty;
        this.examDate = examDate;
    }

    // EXISTING CONSTRUCTOR - Used when creating a NEW subject (ID will be -1 until saved)
    public Subject(String name, int difficulty, String examDate) {
        this.id = -1; // Default ID indicates this subject has NOT been saved to the DB yet.
        this.name = name;
        this.difficulty = difficulty;
        this.examDate = examDate;
    }

    // Phase 2: NEW GETTER/SETTER for ID (Fixes "cannot resolve method getId")
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    // EXISTING GETTERS
    public String getName() { return name; }
    public int getDifficulty() { return difficulty; }
    public String getExamDate() { return examDate; }

    // EXISTING SETTERS
    public void setName(String name) { this.name = name; }
    public void setDifficulty(int difficulty) { this.difficulty = difficulty; }
    public void setExamDate(String examDate) { this.examDate = examDate; }

    @Override
    public String toString() {
        if (examDate != null && !examDate.trim().isEmpty()) {
            return String.format("%s (Difficulty: %d) - Exam: %s", name, difficulty, examDate);
        } else {
            return String.format("%s (Difficulty: %d)", name, difficulty);
        }
    }
}
