package com.cogniquest.cogniquest.dto;

public class AllocationSummary {
    private int totalAllocated;
    private int newStudentsCreated;
    private String message;

    public AllocationSummary(int totalAllocated, int newStudentsCreated, String message) {
        this.totalAllocated = totalAllocated;
        this.newStudentsCreated = newStudentsCreated;
        this.message = message;
    }

    public int getTotalAllocated() {
        return totalAllocated;
    }

    public int getNewStudentsCreated() {
        return newStudentsCreated;
    }

    public String getMessage() {
        return message;
    }
}