package com.cogniquest.cogniquest.model;

import jakarta.persistence.*;

@Entity
@Table(name = "APP_USER")
public class AppUser {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Login for Teacher (e.g., 'thushara')
    @Column(unique = true)
    private String username;

    // Roll number for Student lookup (e.g., 'S202501')
    @Column(unique = true)
    private String rollNumber;

    // Teacher password hash (MOCK: storing plaintext for now)
    private String passwordHash;

    // Role differentiation: "teacher" or "student"
    @Column(nullable = false)
    private String role;

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getRollNumber() {
        return rollNumber;
    }

    public void setRollNumber(String rollNumber) {
        this.rollNumber = rollNumber;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }
}