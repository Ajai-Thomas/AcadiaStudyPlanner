package com.acadia.acadiastudyplanner.model;

public class Task {
    private String name;
    private String subject;
    private String date;
    private String duration;

    public Task(String name, String subject, String date, String duration) {
        this.name = name;
        this.subject = subject;
        this.date = date;
        this.duration = duration;
    }

    public String getName() {
        return name;
    }

    public String getSubject() {
        return subject;
    }

    public String getDate() {
        return date;
    }

    public String getDuration() {
        return duration;
    }
}
