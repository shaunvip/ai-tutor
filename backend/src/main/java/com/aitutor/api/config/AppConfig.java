package com.aitutor.api.config;

import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

import java.net.http.HttpClient;
import java.time.Duration;

@Configuration
@EnableConfigurationProperties(AiTutorProperties.class)
public class AppConfig {

    private static final Duration WORKER_TIMEOUT = Duration.ofMinutes(5);
    private static final Duration WORKER_READ_TIMEOUT = Duration.ofMinutes(5);

    @Bean
    RestClient workerRestClient(AiTutorProperties properties) {
        HttpClient httpClient = HttpClient.newBuilder()
                .connectTimeout(WORKER_TIMEOUT)
                .version(HttpClient.Version.HTTP_1_1)
                .build();
        JdkClientHttpRequestFactory requestFactory = new JdkClientHttpRequestFactory(httpClient);
        requestFactory.setReadTimeout(WORKER_READ_TIMEOUT);

        return RestClient.builder()
                .baseUrl(properties.worker().baseUrl())
                .requestFactory(requestFactory)
                .defaultHeader("X-AI-Tutor-Worker-Token", properties.worker().internalToken())
                .build();
    }
}
