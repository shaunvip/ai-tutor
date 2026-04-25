package com.aitutor.api.worker;

public record PlanStepResult(
        int stepOrder,
        String title,
        int plannedStartMinute,
        int plannedEndMinute
) {
}
