package com.cogniquest.cogniquest.repository;

import com.cogniquest.cogniquest.model.CourseAllocation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface CourseAllocationRepository extends JpaRepository<CourseAllocation, Long> {

    boolean existsByStudentRollNumberAndSyllabusId(String studentRollNumber, Long syllabusId);

    int countBySyllabusId(Long syllabusId);

    // NEW: Fetch all allocations for a specific student (Used by Student Dashboard)
    List<CourseAllocation> findByStudentRollNumber(String studentRollNumber);
}