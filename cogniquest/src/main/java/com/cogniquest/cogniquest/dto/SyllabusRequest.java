package com.cogniquest.cogniquest.dto;

public class SyllabusRequest {
    private String courseName;
    private String syllabusText;

    // Getters and Setters
    public String getCourseName() {
        return courseName;
    }

    public void setCourseName(String courseName) {
        this.courseName = courseName;
    }

    public String getSyllabusText() {
        return syllabusText;
    }

    public void setSyllabusText(String syllabusText) {
        this.syllabusText = syllabusText;
    }
}
