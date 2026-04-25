package com.aitutor.api.config;

import java.net.http.HttpClient;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

@Configuration
@EnableConfigurationProperties(AiTutorProperties.class)
public class AppConfig {

    @Bean
    RestClient workerRestClient(AiTutorProperties properties) {
        HttpClient httpClient = HttpClient.newBuilder()
                .connectTimeout(AppConstants.WORKER_CONNECT_TIMEOUT)
                .version(HttpClient.Version.HTTP_1_1)
                .build();
        JdkClientHttpRequestFactory requestFactory = new JdkClientHttpRequestFactory(httpClient);
        requestFactory.setReadTimeout(AppConstants.WORKER_READ_TIMEOUT);

        return RestClient.builder()
                .baseUrl(properties.worker().baseUrl())
                .requestFactory(requestFactory)
                .defaultHeader(AppConstants.WORKER_TOKEN_HEADER, properties.worker().internalToken())
                .build();
    }
}
