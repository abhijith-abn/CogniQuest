package com.cogniquest.cogniquest.dto;

import java.util.List;

public class AllocationRequest {
    private List<String> rollNumbers;

    public List<String> getRollNumbers() {
        return rollNumbers;
    }

    public void setRollNumbers(List<String> rollNumbers) {
        this.rollNumbers = rollNumbers;
    }
}