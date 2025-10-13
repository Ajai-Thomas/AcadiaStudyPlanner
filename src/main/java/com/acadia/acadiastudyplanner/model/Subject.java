package com.acadia.acadiastudyplanner.model;

public class Subject {
    private String name;
    private int difficulty;
    private String examDate;

    public Subject(String name, int difficulty, String examDate) {
        this.name = name;
        this.difficulty = difficulty;
        this.examDate = examDate;
    }

    public String getName() { return name; }
    public int getDifficulty() { return difficulty; }
    public String getExamDate() { return examDate; }

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

