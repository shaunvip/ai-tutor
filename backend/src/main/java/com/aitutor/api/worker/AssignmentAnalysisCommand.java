package com.aitutor.api.worker;

public record AssignmentAnalysisCommand(
        String assignmentId,
        String assetPath,
        String subject,
        int gradeLevel,
        int age
) {
}
