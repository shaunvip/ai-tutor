package com.aitutor.api.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
@EnableConfigurationProperties(AiTutorProperties.class)
public class AppConfig {

    @Bean
    RestClient workerRestClient(AiTutorProperties properties) {
        return RestClient.builder()
                .baseUrl(properties.worker().baseUrl())
                .build();
    }
}
