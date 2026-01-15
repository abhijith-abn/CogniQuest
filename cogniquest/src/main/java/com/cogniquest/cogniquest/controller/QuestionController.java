package com.cogniquest.cogniquest.controller;

import com.cogniquest.cogniquest.model.Question;
import com.cogniquest.cogniquest.service.QuestionService;
import com.cogniquest.cogniquest.dto.QuestionRequest;
import com.cogniquest.cogniquest.dto.QuestionReviewRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/question")
public class QuestionController {

    private final QuestionService questionService;

    public QuestionController(QuestionService questionService) {
        this.questionService = questionService;
    }

    // 1. POST: Generate questions for a single topic and return them for review (NO SAVE)
    // Request Body: { "topicId": 42 }
    @PostMapping("/generate")
    public ResponseEntity<List<Question>> generateQuestions(@RequestBody QuestionRequest request) {
        try {
            // Service method now returns questions without saving them
            List<Question> generatedQuestions = questionService.generateQuestionsForReview(request.getTopicId());
            return ResponseEntity.ok(generatedQuestions);
        } catch (Exception e) {
            System.err.println("Error generating questions for topic: " + e.getMessage());
            return ResponseEntity.badRequest().body(null);
        }
    }

    // 2. PUT: Save the reviewed questions permanently
    // Request Body: { "questions": [...] }
    @PutMapping("/confirm-and-save/{topicId}")
    public ResponseEntity<List<Question>> confirmAndSaveQuestions(@PathVariable Long topicId, @RequestBody QuestionReviewRequest request) {
        try {
            // Service method handles persistence and cleanup
            List<Question> savedQuestions = questionService.confirmAndSaveQuestions(topicId, request.getQuestions());
            return ResponseEntity.ok(savedQuestions);
        } catch (Exception e) {
            System.err.println("Error confirming and saving questions for topic " + topicId + ": " + e.getMessage());
            return ResponseEntity.badRequest().body(null);
        }
    }
}