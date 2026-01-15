package com.cogniquest.cogniquest.model;

import jakarta.persistence.*;
import java.util.List;

@Entity
@Table(name = "TOPIC")
public class Topic {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    // CRITICAL: Foreign Key linking Topic back to Syllabus
    @Column(name = "syllabus_id")
    private Long syllabusId;

    // Links to Question (Module 2 dependency)
    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "topic_id")
    private List<Question> questions;

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Long getSyllabusId() {
        return syllabusId;
    }

    public void setSyllabusId(Long syllabusId) {
        this.syllabusId = syllabusId;
    }

    public List<Question> getQuestions() {
        return questions;
    }

    public void setQuestions(List<Question> questions) {
        this.questions = questions;
    }
}