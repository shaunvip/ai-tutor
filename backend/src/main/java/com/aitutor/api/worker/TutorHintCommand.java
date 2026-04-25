package com.aitutor.api.worker;

public record TutorHintCommand(
        String sessionId,
        String mode,
        String content
) {
}
