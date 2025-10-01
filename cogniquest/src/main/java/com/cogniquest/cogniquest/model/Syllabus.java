package com.cogniquest.cogniquest.model;

import jakarta.persistence.*;
import java.util.List;

@Entity
public class Syllabus {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String syllabusText;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "syllabus_id")
    private List<Topic> topics;

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getSyllabusText() {
        return syllabusText;
    }

    public void setSyllabusText(String syllabusText) {
        this.syllabusText = syllabusText;
    }

    public List<Topic> getTopics() {
        return topics;
    }

    public void setTopics(List<Topic> topics) {
        this.topics = topics;
    }
}