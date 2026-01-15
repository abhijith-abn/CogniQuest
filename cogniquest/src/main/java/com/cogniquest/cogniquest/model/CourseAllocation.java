package com.cogniquest.cogniquest.model;
import jakarta.persistence.*;

@Entity
@Table(name = "COURSE_ALLOCATION")
public class CourseAllocation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Foreign Key to the Syllabus/Course
    @Column(name = "syllabus_id", nullable = false)
    private Long syllabusId;

    // The student identifier used for access control (M:M bridge)
    @Column(nullable = false)
    private String studentRollNumber;

    // Foreign Key to the teacher who made the allocation
    @Column(nullable = false)
    private Long teacherId;

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

    public String getStudentRollNumber() {
        return studentRollNumber;
    }

    public void setStudentRollNumber(String studentRollNumber) {
        this.studentRollNumber = studentRollNumber;
    }

    public Long getTeacherId() {
        return teacherId;
    }

    public void setTeacherId(Long teacherId) {
        this.teacherId = teacherId;
    }
}
