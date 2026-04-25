package com.aitutor.api.worker;

public record FocusAnalysisResult(
        boolean lookingAway,
        boolean alert,
        double confidence,
        String reason,
        String alertMessage
) {
}
