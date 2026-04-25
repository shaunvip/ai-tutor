package com.aitutor.api.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "ai-tutor")
public record AiTutorProperties(Storage storage, Worker worker) {

    public record Storage(String localRoot) {
    }

    public record Worker(String baseUrl, String internalToken) {
    }
}
