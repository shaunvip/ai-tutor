package com.aitutor.api.worker;

public record TutorHintResult(
        String content,
        double confidence,
        String provider
) {
}
