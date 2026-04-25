package com.aitutor.api.assignment;

record CreateAssignmentRequest(String subject) {
}

record AssignmentResponse(
        String id,
        String status,
        String subject,
        String taskType,
        Integer estimatedWordCount,
        Integer questionCount,
        Integer estimatedTotalMinutes,
        Double confidence,
        String summary,
        java.util.List<AssignmentPlanStepResponse> steps
) {
}

record AssignmentPlanStepResponse(
        int stepOrder,
        String title,
        int plannedStartMinute,
        int plannedEndMinute
) {
}
