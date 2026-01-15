package com.cogniquest.cogniquest.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "STUDENT_PROGRESS")
public class StudentProgress {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Foreign Key to the Student's APP_USER record
    @Column(name = "student_id", nullable = false)
    private Long studentId;

    // Foreign Key to the Topic being assessed
    @Column(name = "topic_id", nullable = false)
    private Long topicId;

    // Status for the Easy Level (NOT_ATTEMPTED, PASSED, FAILED)
    @Column(nullable = false)
    private String easyStatus = "NOT_ATTEMPTED";

    // Status for the Intermediate Level
    @Column(nullable = false)
    private String intermediateStatus = "NOT_ATTEMPTED";

    // Status for the Advanced Level
    @Column(nullable = false)
    private String advancedStatus = "NOT_ATTEMPTED";

    private LocalDateTime lastAttemptDate;

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getStudentId() {
        return studentId;
    }

    public void setStudentId(Long studentId) {
        this.studentId = studentId;
    }

    public Long getTopicId() {
        return topicId;
    }

    public void setTopicId(Long topicId) {
        this.topicId = topicId;
    }

    public String getEasyStatus() {
        return easyStatus;
    }

    public void setEasyStatus(String easyStatus) {
        this.easyStatus = easyStatus;
    }

    public String getIntermediateStatus() {
        return intermediateStatus;
    }

    public void setIntermediateStatus(String intermediateStatus) {
        this.intermediateStatus = intermediateStatus;
    }

    public String getAdvancedStatus() {
        return advancedStatus;
    }

    public void setAdvancedStatus(String advancedStatus) {
        this.advancedStatus = advancedStatus;
    }

    public LocalDateTime getLastAttemptDate() {
        return lastAttemptDate;
    }

    public void setLastAttemptDate(LocalDateTime lastAttemptDate) {
        this.lastAttemptDate = lastAttemptDate;
    }
}
