package com.aitutor.api.worker;

public record ProgressAnalysisCommand(
        String sessionId,
        String assignmentId,
        String progressAssetPath,
        int expectedMinute
) {
}
