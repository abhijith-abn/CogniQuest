package com.cogniquest.cogniquest.service;

import com.cogniquest.cogniquest.dto.QuizSubmission;
import com.cogniquest.cogniquest.model.AppUser;
import com.cogniquest.cogniquest.model.Question;
import com.cogniquest.cogniquest.model.StudentProgress;
import com.cogniquest.cogniquest.model.Topic;
import com.cogniquest.cogniquest.model.Difficulty;
import com.cogniquest.cogniquest.repository.AppUserRepository;
import com.cogniquest.cogniquest.repository.QuestionRepository;
import com.cogniquest.cogniquest.repository.StudentProgressRepository;
import com.cogniquest.cogniquest.repository.CourseAllocationRepository;
import com.cogniquest.cogniquest.repository.TopicRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

@Service
public class QuizService {

    private final AppUserRepository appUserRepository;
    private final QuestionRepository questionRepository;
    private final StudentProgressRepository progressRepository;
    private final CourseAllocationRepository allocationRepository;
    private final TopicRepository topicRepository;

    public QuizService(AppUserRepository appUserRepository, QuestionRepository questionRepository,
                       StudentProgressRepository progressRepository, CourseAllocationRepository allocationRepository,
                       TopicRepository topicRepository) {
        this.appUserRepository = appUserRepository;
        this.questionRepository = questionRepository;
        this.progressRepository = progressRepository;
        this.allocationRepository = allocationRepository;
        this.topicRepository = topicRepository;
    }

    // --- 1. QUIZ START LOGIC (Adaptive Retrieval) ---

    public List<Question> getNextQuizBatch(Long studentId, Long topicId) {
        // 1. Fetch Student Identity and Authorization Status
        AppUser student = appUserRepository.findById(studentId)
                .orElseThrow(() -> new NoSuchElementException("Student not found."));

        // CRITICAL CHECK: Ensure student is allocated to this course (Module 3 requirement)
        Topic topic = topicRepository.findById(topicId)
                .orElseThrow(() -> new NoSuchElementException("Topic not found."));

        if (!isStudentAllocated(student.getRollNumber(), topic.getSyllabusId())) {
            throw new SecurityException("Student is not allocated to this course/topic.");
        }

        // 2. Determine Required Difficulty Level
        StudentProgress progress = getOrCreateProgress(studentId, topicId);
        Difficulty requiredDifficulty = determineNextDifficulty(progress);

        // 3. Retrieve Questions (Always retrieve 3 questions as per rule)
        List<Question> batch = questionRepository.findByTopicId(topicId).stream()
                .filter(q -> q.getDifficulty().equals(requiredDifficulty))
                .collect(Collectors.collectingAndThen(Collectors.toList(), list -> {
                    Collections.shuffle(list);
                    return list.subList(0, Math.min(list.size(), 3));
                }));

        if (batch.isEmpty() && requiredDifficulty != null) {
            // If there are no questions for the next level, treat it as complete.
            return Collections.emptyList();
        }

        return batch;
    }

    // --- 2. QUIZ SUBMISSION LOGIC (Grading and Progress Update) ---

    @Transactional
    public StudentProgress submitQuiz(Long studentId, Long topicId, QuizSubmission submission) {
        StudentProgress progress = getOrCreateProgress(studentId, topicId);
        Difficulty attemptedDifficulty = determineNextDifficulty(progress); // Difficulty they just attempted

        // 1. Score the Quiz (Mock Logic) - Assume pass is 2 out of 3.
        long correctCount = submission.getSubmittedAnswers().stream().filter(ans -> ans.isCorrect()).count();
        boolean passed = correctCount >= 2;

        // 2. Update Status based on Attempted Difficulty
        String newStatus = passed ? "PASSED" : "FAILED";

        if (attemptedDifficulty == Difficulty.EASY) {
            progress.setEasyStatus(newStatus);
        } else if (attemptedDifficulty == Difficulty.INTERMEDIATE) {
            progress.setIntermediateStatus(newStatus);
        } else if (attemptedDifficulty == Difficulty.ADVANCED) {
            progress.setAdvancedStatus(newStatus);
        }

        progress.setLastAttemptDate(LocalDateTime.now());

        // 3. Save and Return the updated progress record
        return progressRepository.save(progress);
    }

    // --- 3. HELPER METHODS ---

    private StudentProgress getOrCreateProgress(Long studentId, Long topicId) {
        return progressRepository.findByStudentIdAndTopicId(studentId, topicId)
                .orElseGet(() -> {
                    StudentProgress newProgress = new StudentProgress();
                    newProgress.setStudentId(studentId);
                    newProgress.setTopicId(topicId);
                    return progressRepository.save(newProgress);
                });
    }

    private Difficulty determineNextDifficulty(StudentProgress progress) {
        if ("PASSED".equals(progress.getEasyStatus()) && !"PASSED".equals(progress.getIntermediateStatus())) {
            return Difficulty.INTERMEDIATE;
        }
        if ("PASSED".equals(progress.getIntermediateStatus()) && !"PASSED".equals(progress.getAdvancedStatus())) {
            return Difficulty.ADVANCED;
        }
        if (!"PASSED".equals(progress.getEasyStatus())) {
            return Difficulty.EASY;
        }
        // All levels passed or no levels left to attempt
        return null;
    }

    private boolean isStudentAllocated(String rollNumber, Long syllabusId) {
        // Query the Allocation table using the student's roll number and the course ID
        return allocationRepository.existsByStudentRollNumberAndSyllabusId(rollNumber, syllabusId);
    }
}