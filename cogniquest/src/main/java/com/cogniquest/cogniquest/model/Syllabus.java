package com.cogniquest.cogniquest.model;

import jakarta.persistence.*;
import java.util.List;

@Entity
@Table(name = "SYLLABUS")
public class Syllabus {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Stores the name of the subject/course (Lean model)
    @Column(nullable = false)
    private String courseName;

    // Tracks if topics have been reviewed and finalized (false = needs review, true = ready for Qs)
    @Column(nullable = false)
    private Boolean isApproved = false;

    // Links to the Teacher who created this syllabus (Placeholder ID for now)
    @Column(name = "teacher_id")
    private Long teacherId;

    // One Syllabus has Many Topics. Cascade ensures topics are saved/deleted with the Syllabus.
    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "syllabus_id") // This is the Foreign Key column in the Topic table
    private List<Topic> topics;

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCourseName() {
        return courseName;
    }

    public void setCourseName(String courseName) {
        this.courseName = courseName;
    }

    public Boolean getIsApproved() {
        return isApproved;
    }

    public void setIsApproved(Boolean approved) {
        isApproved = approved;
    }

    public Long getTeacherId() {
        return teacherId;
    }

    public void setTeacherId(Long teacherId) {
        this.teacherId = teacherId;
    }

    public List<Topic> getTopics() {
        return topics;
    }

    public void setTopics(List<Topic> topics) {
        this.topics = topics;
    }
}