package com.cogniquest.cogniquest.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "QUIZ")
public class Quiz {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Foreign Key to the Syllabus/Course
    @Column(name = "syllabus_id", nullable = false)
    private Long syllabusId;

    // Unique Exam Identifier (Used by the Student)
    @Column(unique = true, nullable = false)
    private String examId;

    // Password to enter the exam (Set by Teacher)
    @Column(nullable = false)
    private String examPassword;

    @Column(nullable = false)
    private LocalDateTime dateCreated = LocalDateTime.now();

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getSyllabusId() {
        return syllabusId;
    }

    public void setSyllabusId(Long syllabusId) {
        this.syllabusId = syllabusId;
    }

    public String getExamId() {
        return examId;
    }

    public void setExamId(String examId) {
        this.examId = examId;
    }

    public String getExamPassword() {
        return examPassword;
    }

    public void setExamPassword(String examPassword) {
        this.examPassword = examPassword;
    }

    public LocalDateTime getDateCreated() {
        return dateCreated;
    }

    public void setDateCreated(LocalDateTime dateCreated) {
        this.dateCreated = dateCreated;
    }
}
   