package com.cogniquest.cogniquest.dto;

import com.cogniquest.cogniquest.model.Topic;
import java.util.List;

public class TopicApprovalRequest {
    private List<Topic> topics;

    // Getters and Setters
    public List<Topic> getTopics() {
        return topics;
    }

    public void setTopics(List<Topic> topics) {
        this.topics = topics;
    }
}