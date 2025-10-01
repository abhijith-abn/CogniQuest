package com.cogniquest.cogniquest.repository;

import com.cogniquest.cogniquest.model.Syllabus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SyllabusRepository extends JpaRepository<Syllabus, Long> {
}