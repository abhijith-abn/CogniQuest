package com.cogniquest.cogniquest.controller;

import com.cogniquest.cogniquest.dto.SyllabusRequest;
import com.cogniquest.cogniquest.model.Syllabus;
import com.cogniquest.cogniquest.service.SyllabusService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;

@RestController
@RequestMapping("/api/syllabus")
@CrossOrigin(origins = "http://localhost:3000") // This is crucial for local development with React
public class SyllabusController {

    private final SyllabusService syllabusService;

    @Autowired
    public SyllabusController(SyllabusService syllabusService) {
        this.syllabusService = syllabusService;
    }

    @PostMapping("/process")
    public ResponseEntity<Syllabus> processSyllabus(@RequestBody SyllabusRequest syllabusRequest) {
        Syllabus processedSyllabus = syllabusService.processSyllabus(syllabusRequest);
        return ResponseEntity.ok(processedSyllabus);
    }
}