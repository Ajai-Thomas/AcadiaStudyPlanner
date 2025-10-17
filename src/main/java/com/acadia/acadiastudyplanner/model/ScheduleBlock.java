package com.acadia.acadiastudyplanner.model;

/**
 * Model class used specifically for Gson parsing of the AI's JSON output.
 */
public class ScheduleBlock {
    // Fields must exactly match the JSON keys defined in the LLM responseSchema
    private String day;
    private String subject_name;
    private String task_title;
    private String time_start;
    private String time_end;

    // Getters are required for Gson
    public String getDay() { return day; }
    public String getSubjectName() { return subject_name; }
    public String getTaskTitle() { return task_title; }
    public String getTimeStart() { return time_start; }
    public String getTimeEnd() { return time_end; }
}
