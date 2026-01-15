package com.cogniquest.cogniquest.model;

import jakarta.persistence.*;

@Entity
public class Question {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // CRITICAL: Foreign Key linking Question back to Topic
    @Column(name = "topic_id")
    private Long topicId; // <-- ADDED THIS FIELD

    @Column(nullable = false, columnDefinition = "TEXT")
    private String questionText;

    // Stores the multiple-choice options in JSON format
    @Column(columnDefinition = "TEXT")
    private String optionsJson;

    @Column(nullable = false)
    private String correctAnswerText;

    @Enumerated(EnumType.STRING)
    private Difficulty difficulty;

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getTopicId() { // <-- ADDED GETTER/SETTER
        return topicId;
    }

    public void setTopicId(Long topicId) { // <-- ADDED GETTER/SETTER
        this.topicId = topicId;
    }

    public String getQuestionText() {
        return questionText;
    }

    public void setQuestionText(String questionText) {
        this.questionText = questionText;
    }

    public String getOptionsJson() {
        return optionsJson;
    }

    public void setOptionsJson(String optionsJson) {
        this.optionsJson = optionsJson;
    }

    public String getCorrectAnswerText() {
        return correctAnswerText;
    }

    public void setCorrectAnswerText(String correctAnswerText) {
        this.correctAnswerText = correctAnswerText;
    }

    public Difficulty getDifficulty() {
        return difficulty;
    }

    public void setDifficulty(Difficulty difficulty) {
        this.difficulty = difficulty;
    }
}