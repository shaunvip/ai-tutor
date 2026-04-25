package com.aitutor.api.worker;

import java.util.List;

public record AssignmentAnalysisResult(
        String subject,
        String taskType,
        int estimatedWordCount,
        int questionCount,
        int estimatedTotalMinutes,
        double confidence,
        List<PlanStepResult> steps,
        String summary
) {

    public static AssignmentAnalysisResult fallback(String subject, int gradeLevel, int age) {
        int baseMinutes = Math.max(12, Math.min(35, 8 + gradeLevel * 3));
        return new AssignmentAnalysisResult(
                subject == null || subject.isBlank() ? "general" : subject,
                "worksheet",
                120,
                5,
                baseMinutes,
                0.25,
                List.of(
                        new PlanStepResult(1, "Read instructions", 0, 3),
                        new PlanStepResult(2, "Finish first half", 3, Math.max(8, baseMinutes / 2)),
                        new PlanStepResult(3, "Finish remaining work", Math.max(8, baseMinutes / 2), Math.max(10, baseMinutes - 3)),
                        new PlanStepResult(4, "Review answers", Math.max(10, baseMinutes - 3), baseMinutes)
                ),
                "Fallback plan created because worker analysis was unavailable"
        );
    }
}
