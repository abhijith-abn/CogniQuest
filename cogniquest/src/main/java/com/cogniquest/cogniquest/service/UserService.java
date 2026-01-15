package com.cogniquest.cogniquest.service;

import com.cogniquest.cogniquest.dto.AllocationSummary;
import com.cogniquest.cogniquest.model.AppUser;
import com.cogniquest.cogniquest.model.CourseAllocation;
import com.cogniquest.cogniquest.repository.AppUserRepository;
import com.cogniquest.cogniquest.repository.CourseAllocationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
public class UserService {

    private final AppUserRepository appUserRepository;
    private final CourseAllocationRepository allocationRepository;

    public UserService(AppUserRepository appUserRepository, CourseAllocationRepository allocationRepository) {
        this.appUserRepository = appUserRepository;
        this.allocationRepository = allocationRepository;
    }

    /**
     * Authenticates a student by Roll Number and Password, checking role and hash match.
     * Searches the DB using multiple case variations of the input roll number.
     */
    public AppUser authenticateStudent(String rollNumber, String password) {
        if (rollNumber == null || password == null) return null;

        String inputRollNumber = rollNumber.trim();
        AppUser user = null;

        // 1. Attempt lookup by exact input case
        user = appUserRepository.findByRollNumber(inputRollNumber);

        // 2. Fallback: Check UPPERCASE version (This is necessary to catch inconsistent capitalization)
        if (user == null) {
            user = appUserRepository.findByRollNumber(inputRollNumber.toUpperCase().trim());
        }

        // 3. Fallback: Check LOWERCASE version
        if (user == null) {
            user = appUserRepository.findByRollNumber(inputRollNumber.toLowerCase().trim());
        }

        // --- DEBUGGING LOGS (Check your Spring Boot Console) ---
        if (user == null) {
            System.out.println("[AUTH] Failed: No user found with Roll Number: " + inputRollNumber);
        } else {
            System.out.println("[AUTH] User Found: " + user.getRollNumber());
            System.out.println("[AUTH] DB Role: '" + user.getRole() + "' | DB Password: '" + user.getPasswordHash() + "'");
            System.out.println("[AUTH] Input Password: '" + password + "'");

            // 4. Validate role (Case-Insensitive) and password
            if (user.getRole() != null && user.getRole().equalsIgnoreCase("student")) {
                if (password.trim().equals(user.getPasswordHash().trim())) {
                    System.out.println("[AUTH] Success!");
                    return user; // Authentication successful
                } else {
                    System.out.println("[AUTH] Failed: Password Mismatch.");
                }
            } else {
                System.out.println("[AUTH] Failed: User is not a student (Role: " + user.getRole() + ").");
            }
        }

        return null; // Authentication failed
    }

    /**
     * Processes allocation based on CSV lines, creating users if necessary.
     */
    @Transactional
    public AllocationSummary allocateStudentsToCourse(Long syllabusId, List<String> csvLines, Long teacherId) {
        int newStudentsCreated = 0;
        List<CourseAllocation> newAllocations = new ArrayList<>();

        // Use provided CSV lines. Roll numbers are standardized to UPPERCASE on storage.
        List<String> linesToProcess = (csvLines != null && !csvLines.isEmpty() && !(csvLines.size() == 1 && csvLines.get(0).trim().isEmpty()))
                ? csvLines
                : List.of();

        for (String line : linesToProcess) {
            String[] parts = line.split(",");
            String rawRollNumber = parts[0].trim();
            String standardizedRollNumber = rawRollNumber.toUpperCase(); // Standardize to UPPERCASE
            String rawPassword = parts.length > 1 ? parts[1].trim() : rawRollNumber; // Use roll number as default password if none provided

            if (rawRollNumber.isEmpty()) continue;

            // 1. Ensure student user exists (Lookup by standardized roll number)
            AppUser student = appUserRepository.findByRollNumber(standardizedRollNumber);

            if (student == null) {
                // If not found (e.g., if the user existed with a different case originally), create new
                student = new AppUser();
                student.setRollNumber(standardizedRollNumber);
                student.setRole("student");
                student.setUsername(standardizedRollNumber);
                student.setPasswordHash(rawPassword);
                appUserRepository.save(student);
                newStudentsCreated++;
            } else {
                // Update the password hash for existing users
                student.setPasswordHash(rawPassword);
                appUserRepository.save(student);
            }

            String finalRollNumber = student.getRollNumber();

            // 2. Allocate to course
            if (!allocationRepository.existsByStudentRollNumberAndSyllabusId(finalRollNumber, syllabusId)) {
                CourseAllocation allocation = new CourseAllocation();
                allocation.setSyllabusId(syllabusId);
                allocation.setStudentRollNumber(finalRollNumber);
                allocation.setTeacherId(teacherId);
                newAllocations.add(allocation);
            }
        }

        allocationRepository.saveAll(newAllocations);

        return new AllocationSummary(
                newAllocations.size(),
                newStudentsCreated,
                "Allocation processed. New students created: " + newStudentsCreated
        );
    }
}