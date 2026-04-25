package com.aitutor.api.worker;

import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

@Component
public class WorkerClient {

    private final RestClient workerRestClient;

    public WorkerClient(RestClient workerRestClient) {
        this.workerRestClient = workerRestClient;
    }

    public AssignmentAnalysisResult analyzeAssignment(AssignmentAnalysisCommand command) {
        try {
            AssignmentAnalysisResult result = workerRestClient.post()
                    .uri("/internal/analyze-assignment")
                    .body(command)
                    .retrieve()
                    .body(AssignmentAnalysisResult.class);
            return result == null
                    ? AssignmentAnalysisResult.fallback(command.subject(), command.gradeLevel(), command.age())
                    : result;
        } catch (RestClientException ex) {
            return AssignmentAnalysisResult.fallback(command.subject(), command.gradeLevel(), command.age());
        }
    }

    public ProgressAnalysisResult analyzeProgress(ProgressAnalysisCommand command) {
        try {
            ProgressAnalysisResult result = workerRestClient.post()
                    .uri("/internal/analyze-progress")
                    .body(command)
                    .retrieve()
                    .body(ProgressAnalysisResult.class);
            return result == null
                    ? new ProgressAnalysisResult(0, 0.2, 0, "Worker returned an empty response")
                    : result;
        } catch (RestClientException ex) {
            return new ProgressAnalysisResult(0, 0.2, 0, "Worker unavailable; saved capture for manual review");
        }
    }
}
