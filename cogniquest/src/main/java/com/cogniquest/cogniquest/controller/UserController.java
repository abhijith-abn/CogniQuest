package com.cogniquest.cogniquest.controller;

import com.cogniquest.cogniquest.dto.AllocationRequest;
import com.cogniquest.cogniquest.dto.AllocationSummary;
import com.cogniquest.cogniquest.dto.LoginRequest; // Using the new DTO
import com.cogniquest.cogniquest.service.UserService;
import com.cogniquest.cogniquest.model.AppUser;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/api/user")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    // Authenticate Student (Roll No + Password)
    @PostMapping("/auth/student")
    public ResponseEntity<AppUser> authenticateStudent(@RequestBody LoginRequest credentials) {
        // Log exactly what arrived from the Frontend
        System.out.println("[CONTROLLER] Login Attempt - Roll: " + credentials.getRollNumber() + ", Pass: " + credentials.getPassword());

        AppUser user = userService.authenticateStudent(credentials.getRollNumber(), credentials.getPassword());

        if (user != null) {
            return ResponseEntity.ok(user);
        }
        return ResponseEntity.status(401).body(null);
    }

    @PostMapping("/allocate-students/{syllabusId}")
    public ResponseEntity<AllocationSummary> allocateStudents(@PathVariable Long syllabusId, @RequestBody AllocationRequest request) {
        try {
            AllocationSummary summary = userService.allocateStudentsToCourse(syllabusId, request.getRollNumbers(), 1L);
            return ResponseEntity.ok(summary);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(new AllocationSummary(0, 0, "Error: " + e.getMessage()));
        }
    }
}