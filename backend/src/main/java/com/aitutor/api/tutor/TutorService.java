package com.aitutor.api.tutor;

import com.aitutor.api.worker.TutorHintCommand;
import com.aitutor.api.worker.TutorHintResult;
import com.aitutor.api.worker.WorkerClient;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TutorService {

    private final TutorThreadRepository threads;
    private final TutorMessageRepository messages;
    private final WorkerClient workerClient;

    public TutorService(TutorThreadRepository threads, TutorMessageRepository messages, WorkerClient workerClient) {
        this.threads = threads;
        this.messages = messages;
        this.workerClient = workerClient;
    }

    @Transactional
    public TutorThreadResponse createThread(UUID studentId, CreateTutorThreadRequest request) {
        TutorThread thread = threads.save(new TutorThread(studentId, request.sessionId()));
        return response(thread);
    }

    @Transactional
    public TutorMessageResponse sendMessage(UUID studentId, UUID threadId, SendTutorMessageRequest request) {
        TutorThread thread = findOwned(studentId, threadId);
        TutorMessage userMessage = messages.save(new TutorMessage(thread.getId(), "student", request.content()));
        String response = tutorResponse(thread, request);
        TutorMessage tutorMessage = messages.save(new TutorMessage(thread.getId(), "tutor", response));
        return new TutorMessageResponse(tutorMessage.getId().toString(), userMessage.getRole(), tutorMessage.getRole(), tutorMessage.getContent());
    }

    @Transactional(readOnly = true)
    public List<TutorThreadResponse> listThreads(UUID studentId) {
        return threads.findByStudentIdOrderByCreatedAtDesc(studentId).stream()
                .map(this::response)
                .toList();
    }

    private TutorThread findOwned(UUID studentId, UUID threadId) {
        TutorThread thread = threads.findById(threadId)
                .orElseThrow(() -> new IllegalArgumentException("Tutor thread not found"));
        if (!thread.getStudentId().equals(studentId)) {
            throw new IllegalArgumentException("Tutor thread not found");
        }
        return thread;
    }

    private TutorThreadResponse response(TutorThread thread) {
        return new TutorThreadResponse(
                thread.getId().toString(),
                thread.getSessionId() == null ? null : thread.getSessionId().toString()
        );
    }

    private String hintFirstResponse(String question, String mode) {
        String normalizedMode = mode == null || mode.isBlank() ? "hint" : mode;
        return switch (normalizedMode) {
            case "check_answer" -> "Let's check it together. First, read your answer once and compare it with the question. What part of the question does your answer directly address?";
            case "explain" -> "Let's understand the question first. Tell me the key words you see, then we will solve one small step at a time.";
            default -> "Let's solve it together. First, underline the important words in the question, then try the first step. What do you think the question is asking?";
        };
    }

    private String tutorResponse(TutorThread thread, SendTutorMessageRequest request) {
        TutorHintResult result = workerClient.generateTutorHint(new TutorHintCommand(
                thread.getSessionId() == null ? null : thread.getSessionId().toString(),
                request.mode(),
                request.content()
        ));
        if (result.content() != null && !result.content().isBlank()) {
            return result.content();
        }
        return hintFirstResponse(request.content(), request.mode());
    }
}
