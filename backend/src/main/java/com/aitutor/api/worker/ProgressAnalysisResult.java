package com.aitutor.api.worker;

public record ProgressAnalysisResult(
        int completionPercent,
        double confidence,
        int behindMinutes,
        String summary
) {
}
