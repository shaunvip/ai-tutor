package com.aitutor.api.session;

import com.aitutor.api.assignment.Assignment;
import com.aitutor.api.assignment.AssignmentService;
import com.aitutor.api.focus.FocusEvent;
import com.aitutor.api.focus.FocusEventRepository;
import com.aitutor.api.storage.StorageService;
import com.aitutor.api.storage.StoredFile;
import com.aitutor.api.worker.ProgressAnalysisCommand;
import com.aitutor.api.worker.ProgressAnalysisResult;
import com.aitutor.api.worker.WorkerClient;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
public class StudySessionService {

    private final StudySessionRepository sessions;
    private final SessionEventRepository events;
    private final ProgressCaptureRepository captures;
    private final FocusEventRepository focusEvents;
    private final AssignmentService assignmentService;
    private final StorageService storageService;
    private final WorkerClient workerClient;

    public StudySessionService(StudySessionRepository sessions, SessionEventRepository events,
                               ProgressCaptureRepository captures, FocusEventRepository focusEvents,
                               AssignmentService assignmentService, StorageService storageService,
                               WorkerClient workerClient) {
        this.sessions = sessions;
        this.events = events;
        this.captures = captures;
        this.focusEvents = focusEvents;
        this.assignmentService = assignmentService;
        this.storageService = storageService;
        this.workerClient = workerClient;
    }

    @Transactional
    public StudySessionResponse start(UUID studentId, StartSessionRequest request) {
        Assignment assignment = assignmentService.findOwned(studentId, request.assignmentId());
        StudySession session = sessions.save(new StudySession(studentId, assignment.getId()));
        events.save(new SessionEvent(session.getId(), "SESSION_STARTED", "{\"assignmentId\":\"" + assignment.getId() + "\"}"));
        return response(session);
    }

    @Transactional(readOnly = true)
    public List<StudySessionResponse> list(UUID studentId) {
        return sessions.findByStudentIdOrderByStartedAtDesc(studentId).stream()
                .map(this::response)
                .toList();
    }

    @Transactional
    public StudySessionResponse completeStep(UUID studentId, UUID sessionId, int stepOrder) {
        StudySession session = findOwned(studentId, sessionId);
        session.markStepComplete(stepOrder);
        events.save(new SessionEvent(session.getId(), "STEP_COMPLETED", "{\"stepOrder\":" + stepOrder + "}"));
        return response(session);
    }

    @Transactional
    public StudySessionResponse complete(UUID studentId, UUID sessionId) {
        StudySession session = findOwned(studentId, sessionId);
        session.complete();
        events.save(new SessionEvent(session.getId(), "SESSION_COMPLETED", "{}"));
        return response(session);
    }

    @Transactional
    public FocusEventResponse addFocusEvent(UUID studentId, UUID sessionId, FocusEventRequest request) {
        StudySession session = findOwned(studentId, sessionId);
        FocusEvent event = focusEvents.save(new FocusEvent(
                session.getId(),
                request.eventType(),
                request.durationSeconds(),
                request.note()
        ));
        events.save(new SessionEvent(session.getId(), "FOCUS_EVENT", "{\"eventType\":\"" + request.eventType() + "\"}"));
        return new FocusEventResponse(event.getId().toString(), request.eventType(), request.durationSeconds(), request.note());
    }

    @Transactional
    public ProgressCaptureResponse uploadProgressCapture(UUID studentId, UUID sessionId, MultipartFile file) {
        StudySession session = findOwned(studentId, sessionId);
        StoredFile stored = storageService.save(file, "progress");
        ProgressCapture capture = captures.save(new ProgressCapture(session.getId(), stored.objectKey(), stored.filePath()));

        int expectedMinute = Math.toIntExact(Math.max(0, Duration.between(session.getStartedAt(), Instant.now()).toMinutes()));
        ProgressAnalysisResult result = workerClient.analyzeProgress(new ProgressAnalysisCommand(
                session.getId().toString(),
                session.getAssignmentId().toString(),
                capture.getFilePath(),
                expectedMinute
        ));

        capture.applyAnalysis(result.completionPercent(), result.confidence(), result.behindMinutes(), result.summary());
        events.save(new SessionEvent(session.getId(), "PROGRESS_CAPTURE_ANALYZED", "{\"captureId\":\"" + capture.getId() + "\"}"));
        return progressResponse(capture);
    }

    private StudySession findOwned(UUID studentId, UUID sessionId) {
        StudySession session = sessions.findById(sessionId)
                .orElseThrow(() -> new IllegalArgumentException("Study session not found"));
        if (!session.getStudentId().equals(studentId)) {
            throw new IllegalArgumentException("Study session not found");
        }
        return session;
    }

    private StudySessionResponse response(StudySession session) {
        List<ProgressCaptureResponse> progress = captures.findBySessionIdOrderByCreatedAtDesc(session.getId()).stream()
                .map(this::progressResponse)
                .toList();

        return new StudySessionResponse(
                session.getId().toString(),
                session.getAssignmentId().toString(),
                session.getStatus().name(),
                session.getCurrentStepOrder(),
                session.getStartedAt().toString(),
                progress
        );
    }

    private ProgressCaptureResponse progressResponse(ProgressCapture capture) {
        return new ProgressCaptureResponse(
                capture.getId().toString(),
                capture.getCompletionPercent(),
                capture.getConfidence(),
                capture.getBehindMinutes(),
                capture.getSummary()
        );
    }
}
