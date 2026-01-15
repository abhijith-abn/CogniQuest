package com.cogniquest.cogniquest.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public class QuestionRequest {

    // The field name used internally
    @JsonProperty("topicId")
    private Long topicId;

    // Getters and Setters (CORRECTED to use 'TopicId')
    public Long getTopicId() {
        return topicId;
    }

    public void setTopicId(Long topicId) {
        this.topicId = topicId;
    }
}