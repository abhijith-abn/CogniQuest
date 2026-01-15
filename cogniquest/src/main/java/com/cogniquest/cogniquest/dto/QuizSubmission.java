package com.cogniquest.cogniquest.dto;

import java.util.List;

public class QuizSubmission {

    // The single topic being assessed in this submission batch
    private Long topicId;

    // A list containing the student's submission data for each question in the batch
    private List<Answer> submittedAnswers;

    /**
     * Inner class representing the student's answer for a single question.
     * Note: The frontend sends the result (isCorrect) for simplified grading logic.
     */
    public static class Answer {
        private Long questionId;
        private String submittedAnswerText;
        private boolean isCorrect; // Frontend sends this result

        public Long getQuestionId() {
            return questionId;
        }

        public void setQuestionId(Long questionId) {
            this.questionId = questionId;
        }

        public String getSubmittedAnswerText() {
            return submittedAnswerText;
        }

        public void setSubmittedAnswerText(String submittedAnswerText) {
            this.submittedAnswerText = submittedAnswerText;
        }

        public boolean isCorrect() {
            return isCorrect;
        }

        public void setCorrect(boolean correct) {
            isCorrect = correct;
        }
    }

    public Long getTopicId() {
        return topicId;
    }

    public void setTopicId(Long topicId) {
        this.topicId = topicId;
    }

    public List<Answer> getSubmittedAnswers() {
        return submittedAnswers;
    }

    public void setSubmittedAnswers(List<Answer> submittedAnswers) {
        this.submittedAnswers = submittedAnswers;
    }
}
