package com.cogniquest.cogniquest.dto;

import com.cogniquest.cogniquest.model.Topic;
import java.util.List;

public class CourseTopicListDTO {
    private Long id;
    private String courseName;
    private Boolean isApproved;
    private List<Topic> topics;

    // This field carries the count from DB to Frontend
    private int allocatedStudentCount;

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getCourseName() { return courseName; }
    public void setCourseName(String courseName) { this.courseName = courseName; }

    public Boolean getIsApproved() { return isApproved; }
    public void setIsApproved(Boolean isApproved) { this.isApproved = isApproved; }

    public List<Topic> getTopics() { return topics; }
    public void setTopics(List<Topic> topics) { this.topics = topics; }

    public int getAllocatedStudentCount() { return allocatedStudentCount; }
    public void setAllocatedStudentCount(int allocatedStudentCount) { this.allocatedStudentCount = allocatedStudentCount; }
}