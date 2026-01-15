package com.cogniquest.cogniquest.repository;

import com.cogniquest.cogniquest.model.StudentProgress;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface StudentProgressRepository extends JpaRepository<StudentProgress, Long> {

    /**
     * Finds a single progress record for a specific student and topic combination.
     * Used by QuizService to check current status (Easy, Intermediate, Advanced)
     * or to create a new record if one doesn't exist.
     */
    Optional<StudentProgress> findByStudentIdAndTopicId(Long studentId, Long topicId);
}
