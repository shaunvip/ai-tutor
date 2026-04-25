package com.aitutor.api.worker;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;

@Component
public class WorkerClient {

    private static final Logger log = LoggerFactory.getLogger(WorkerClient.class);
    private static final int MAX_WORKER_LOG_CHARS = 2_000;

    private final RestClient workerRestClient;
    private final ObjectMapper objectMapper;

    public WorkerClient(RestClient workerRestClient, ObjectMapper objectMapper) {
        this.workerRestClient = workerRestClient;
        this.objectMapper = objectMapper;
    }

    public AssignmentAnalysisResult analyzeAssignment(AssignmentAnalysisCommand command) {
        log.info(
                "calling worker analyze-assignment assignmentId={} assetPath={} subject={} gradeLevel={} age={}",
                command.assignmentId(),
                command.assetPath(),
                command.subject(),
                command.gradeLevel(),
                command.age()
        );
        try {
            AssignmentAnalysisResult result = workerRestClient.post()
                    .uri("/internal/analyze-assignment")
                    .contentType(MediaType.APPLICATION_JSON)
                    .accept(MediaType.APPLICATION_JSON)
                    .body(jsonBody(command))
                    .retrieve()
                    .body(AssignmentAnalysisResult.class);
            log.info("worker analyze-assignment response={}", truncate(result));
            return result == null
                    ? AssignmentAnalysisResult.fallback(command.subject(), command.gradeLevel(), command.age())
                    : result;
        } catch (JsonProcessingException | RestClientException ex) {
            logWorkerFailure("analyze-assignment", ex);
            return AssignmentAnalysisResult.fallback(command.subject(), command.gradeLevel(), command.age());
        }
    }

    public ProgressAnalysisResult analyzeProgress(ProgressAnalysisCommand command) {
        log.info(
                "calling worker analyze-progress sessionId={} assignmentId={} progressAssetPath={} expectedMinute={}",
                command.sessionId(),
                command.assignmentId(),
                command.progressAssetPath(),
                command.expectedMinute()
        );
        try {
            ProgressAnalysisResult result = workerRestClient.post()
                    .uri("/internal/analyze-progress")
                    .contentType(MediaType.APPLICATION_JSON)
                    .accept(MediaType.APPLICATION_JSON)
                    .body(jsonBody(command))
                    .retrieve()
                    .body(ProgressAnalysisResult.class);
            log.info("worker analyze-progress response={}", truncate(result));
            return result == null
                    ? new ProgressAnalysisResult(0, 0.2, 0, "Worker returned an empty response")
                    : result;
        } catch (JsonProcessingException | RestClientException ex) {
            logWorkerFailure("analyze-progress", ex);
            return new ProgressAnalysisResult(0, 0.2, 0, "Worker unavailable; saved capture for manual review");
        }
    }

    public FocusAnalysisResult analyzeFocus(FocusAnalysisCommand command) {
        log.info(
                "calling worker analyze-focus sessionId={} focusAssetPath={}",
                command.sessionId(),
                command.focusAssetPath()
        );
        try {
            FocusAnalysisResult result = workerRestClient.post()
                    .uri("/internal/analyze-focus")
                    .contentType(MediaType.APPLICATION_JSON)
                    .accept(MediaType.APPLICATION_JSON)
                    .body(jsonBody(command))
                    .retrieve()
                    .body(FocusAnalysisResult.class);
            log.info("worker analyze-focus response={}", truncate(result));
            return result == null
                    ? new FocusAnalysisResult(false, false, 0.1, "Worker returned an empty response", "")
                    : result;
        } catch (JsonProcessingException | RestClientException ex) {
            logWorkerFailure("analyze-focus", ex);
            return new FocusAnalysisResult(false, false, 0.1, "Worker unavailable; focus image saved", "");
        }
    }

    public TutorHintResult generateTutorHint(TutorHintCommand command) {
        log.info(
                "calling worker tutor-hint sessionId={} mode={} contentLength={}",
                command.sessionId(),
                command.mode(),
                command.content() == null ? 0 : command.content().length()
        );
        try {
            TutorHintResult result = workerRestClient.post()
                    .uri("/internal/tutor-hint")
                    .contentType(MediaType.APPLICATION_JSON)
                    .accept(MediaType.APPLICATION_JSON)
                    .body(jsonBody(command))
                    .retrieve()
                    .body(TutorHintResult.class);
            log.info("worker tutor-hint response={}", truncate(result));
            return result == null
                    ? new TutorHintResult("", 0.1, "empty-worker-response")
                    : result;
        } catch (JsonProcessingException | RestClientException ex) {
            logWorkerFailure("tutor-hint", ex);
            return new TutorHintResult("", 0.1, "worker-unavailable");
        }
    }

    private String jsonBody(Object command) throws JsonProcessingException {
        String body = objectMapper.writeValueAsString(command);
        log.info("worker request body={}", truncate(body));
        return body;
    }

    private String truncate(Object value) {
        String text = String.valueOf(value);
        if (text.length() <= MAX_WORKER_LOG_CHARS) {
            return text;
        }
        return text.substring(0, MAX_WORKER_LOG_CHARS) + "...";
    }

    private void logWorkerFailure(String operation, Exception ex) {
        if (ex instanceof RestClientResponseException responseException) {
            log.warn(
                    "worker {} failed status={} errorType={} message={} responseBody={}",
                    operation,
                    responseException.getStatusCode(),
                    ex.getClass().getSimpleName(),
                    ex.getMessage(),
                    truncate(responseException.getResponseBodyAsString())
            );
            return;
        }
        log.warn(
                "worker {} failed errorType={} message={}",
                operation,
                ex.getClass().getSimpleName(),
                ex.getMessage()
        );
    }
}
