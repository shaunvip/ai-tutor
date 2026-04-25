package com.aitutor.api.session;

import com.aitutor.api.common.CurrentStudent;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/study-sessions")
public class StudySessionController {

    private final StudySessionService studySessionService;
    private final CurrentStudent currentStudent;

    public StudySessionController(StudySessionService studySessionService, CurrentStudent currentStudent) {
        this.studySessionService = studySessionService;
        this.currentStudent = currentStudent;
    }

    @PostMapping
    StudySessionResponse start(@Valid @RequestBody StartSessionRequest request) {
        return studySessionService.start(currentStudent.id(), request);
    }

    @GetMapping
    List<StudySessionResponse> list() {
        return studySessionService.list(currentStudent.id());
    }

    @PostMapping("/{sessionId}/steps/{stepOrder}/complete")
    StudySessionResponse completeStep(@PathVariable UUID sessionId, @PathVariable int stepOrder) {
        return studySessionService.completeStep(currentStudent.id(), sessionId, stepOrder);
    }

    @PostMapping("/{sessionId}/complete")
    StudySessionResponse complete(@PathVariable UUID sessionId) {
        return studySessionService.complete(currentStudent.id(), sessionId);
    }

    @PostMapping("/{sessionId}/focus-events")
    FocusEventResponse addFocusEvent(@PathVariable UUID sessionId, @RequestBody FocusEventRequest request) {
        return studySessionService.addFocusEvent(currentStudent.id(), sessionId, request);
    }

    @PostMapping(value = "/{sessionId}/progress-captures", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    ProgressCaptureResponse uploadProgressCapture(@PathVariable UUID sessionId, @RequestPart("file") MultipartFile file) {
        return studySessionService.uploadProgressCapture(currentStudent.id(), sessionId, file);
    }

    @PostMapping(value = "/{sessionId}/focus-checks", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    FocusCheckResponse uploadFocusCheck(@PathVariable UUID sessionId, @RequestPart("file") MultipartFile file) {
        return studySessionService.uploadFocusCheck(currentStudent.id(), sessionId, file);
    }
}
