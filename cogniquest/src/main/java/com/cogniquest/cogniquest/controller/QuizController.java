package com.cogniquest.cogniquest.controller;


import com.cogniquest.cogniquest.dto.QuizSubmission;
import com.cogniquest.cogniquest.model.Question;
import com.cogniquest.cogniquest.model.StudentProgress;
import com.cogniquest.cogniquest.service.QuizService;
import com.cogniquest.cogniquest.repository.CourseAllocationRepository; // Needed for count
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;
import java.util.NoSuchElementException;

@RestController
@RequestMapping("/api/quiz")
public class QuizController {

    private final QuizService quizService;
    private final CourseAllocationRepository allocationRepository; // Use Repo directly for simple count

    public QuizController(QuizService quizService, CourseAllocationRepository allocationRepository) {
        this.quizService = quizService;
        this.allocationRepository = allocationRepository;
    }

    // --- Student Quiz Endpoints ---

    // Endpoint to retrieve the next batch of 3 questions based on student progress
    // GET /api/quiz/start/{studentId}/{topicId}
    @GetMapping("/start/{studentId}/{topicId}")
    public ResponseEntity<List<Question>> startQuiz(@PathVariable Long studentId, @PathVariable Long topicId) {
        try {
            List<Question> questions = quizService.getNextQuizBatch(studentId, topicId);
            return ResponseEntity.ok(questions);
        } catch (NoSuchElementException e) {
            // 404 if student or topic not found
            return ResponseEntity.notFound().build();
        } catch (SecurityException e) {
            // 403 if student is not allocated to this course
            return ResponseEntity.status(403).body(Collections.emptyList());
        } catch (Exception e) {
            System.err.println("Error starting quiz: " + e.getMessage());
            return ResponseEntity.internalServerError().body(Collections.emptyList());
        }
    }

    // Endpoint to submit answers and update student progress
    // POST /api/quiz/submit/{studentId}/{topicId}
    @PostMapping("/submit/{studentId}/{topicId}")
    public ResponseEntity<StudentProgress> submitQuiz(@PathVariable Long studentId, @PathVariable Long topicId, @RequestBody QuizSubmission submission) {
        try {
            StudentProgress updatedProgress = quizService.submitQuiz(studentId, topicId, submission);
            return ResponseEntity.ok(updatedProgress);
        } catch (Exception e) {
            System.err.println("Error submitting quiz: " + e.getMessage());
            return ResponseEntity.internalServerError().body(null);
        }
    }

    // --- Teacher/Admin Helper Endpoints ---

    // Endpoint to get the count of allocated students for a syllabus (used in Teacher Dashboard)
    // GET /api/quiz/allocation-count/{syllabusId}
    @GetMapping("/allocation-count/{syllabusId}")
    public ResponseEntity<Integer> getAllocationCount(@PathVariable Long syllabusId) {
        try {
            int count = allocationRepository.countBySyllabusId(syllabusId);
            return ResponseEntity.ok(count);
        } catch (Exception e) {
            return ResponseEntity.ok(0); // Return 0 on error
        }
    }

    // Create Quiz Record (For Exam ID/Password generation - Module 3/4 transition)
    // POST /api/quiz/create-quiz
    // Note: The actual implementation of `createQuiz` logic resides in `UserService` or `QuizService` usually,
    // but for the flow we discussed, we'll keep it simple.
    // If you need a specific DTO for creating the quiz metadata (Exam ID/Pass), we can add it.
    // For now, the frontend calls this endpoint just to get a confirmation or ID.
    @PostMapping("/create-quiz")
    public ResponseEntity<java.util.Map<String, String>> createQuiz(@RequestBody java.util.Map<String, Object> payload) {
        // Mocking the creation of a Quiz record for access control
        // In a full implementation, you would save this to a 'Quiz' table.
        String examId = (String) payload.get("examId");
        String examPassword = (String) payload.get("examPassword");

        // Return the same data to confirm "creation"
        return ResponseEntity.ok(java.util.Map.of(
                "examId", examId,
                "examPassword", examPassword,
                "status", "CREATED"
        ));
    }
}
