package com.cogniquest.cogniquest.controller;


import com.cogniquest.cogniquest.dto.CourseTopicListDTO;
import com.cogniquest.cogniquest.dto.TopicApprovalRequest;
import com.cogniquest.cogniquest.dto.SyllabusRequest;
import com.cogniquest.cogniquest.model.Syllabus;
import com.cogniquest.cogniquest.service.SyllabusService;
import com.cogniquest.cogniquest.repository.CourseAllocationRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;
import java.util.NoSuchElementException;

@RestController
@RequestMapping("/api/syllabus")
public class SyllabusController {

    private final SyllabusService syllabusService;
    private final CourseAllocationRepository allocationRepository;

    // Inject both Service and Repository
    public SyllabusController(SyllabusService syllabusService, CourseAllocationRepository allocationRepository) {
        this.syllabusService = syllabusService;
        this.allocationRepository = allocationRepository;
    }

    @PostMapping("/generate-topics")
    public ResponseEntity<CourseTopicListDTO> generateTopics(@RequestBody SyllabusRequest syllabusRequest) {
        try {
            Syllabus syllabus = syllabusService.processSyllabus(syllabusRequest);
            return ResponseEntity.ok(convertToDto(syllabus));
        } catch (RuntimeException e) {
            System.err.println("Error generating topics: " + e.getMessage());
            return ResponseEntity.badRequest().body(null);
        }
    }

    @PutMapping("/finalize-topics/{syllabusId}")
    public ResponseEntity<CourseTopicListDTO> finalizeTopics(@PathVariable Long syllabusId, @RequestBody TopicApprovalRequest request) {
        try {
            Syllabus finalizedSyllabus = syllabusService.finalizeTopics(syllabusId, request.getTopics());
            return ResponseEntity.ok(convertToDto(finalizedSyllabus));
        } catch (NoSuchElementException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            System.err.println("Error finalizing topics: " + e.getMessage());
            return ResponseEntity.badRequest().body(null);
        }
    }

    // CRITICAL ENDPOINT: Fetches all courses for the dashboard
    @GetMapping
    public ResponseEntity<List<CourseTopicListDTO>> findAllCourses() {
        List<Syllabus> syllabi = syllabusService.findAllSyllabi();
        List<CourseTopicListDTO> dtos = syllabi.stream()
                .map(this::convertToDto) // Convert each syllabus to DTO with count
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    // Helper to map DB entity to JSON DTO and calculate student count
    private CourseTopicListDTO convertToDto(Syllabus syllabus) {
        CourseTopicListDTO dto = new CourseTopicListDTO();
        dto.setId(syllabus.getId());
        dto.setCourseName(syllabus.getCourseName());
        dto.setIsApproved(syllabus.getIsApproved());
        dto.setTopics(syllabus.getTopics());

        // THE FIX: Fetch the count from the CourseAllocationRepository
        if (syllabus.getId() != null) {
            // This calls the method we defined to count the allocated student rows
            int count = allocationRepository.countBySyllabusId(syllabus.getId());
            dto.setAllocatedStudentCount(count);
        } else {
            dto.setAllocatedStudentCount(0);
        }

        return dto;
    }
}