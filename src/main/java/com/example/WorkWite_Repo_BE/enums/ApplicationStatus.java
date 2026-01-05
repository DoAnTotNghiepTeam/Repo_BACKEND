package com.example.WorkWite_Repo_BE.enums;

public enum ApplicationStatus {
    PENDING,
//    APPLIED,
    CV_PASSED,
    INTERVIEW,
    HIRED,
    REJECTED;

    public static ApplicationStatus fromStep(String step) {
        // chuẩn hóa để tránh case-sensitive
        String normalized = step.trim().toUpperCase().replace(" ", "_");
        return switch (normalized) {
//            case "APPLIED" -> APPLIED;
            case "CV_PASSED" -> CV_PASSED;
            case "INTERVIEW" -> INTERVIEW;
            case "HIRED" -> HIRED;
            case "REJECTED" -> REJECTED;
            default -> PENDING; // fallback
        };
    }
}

