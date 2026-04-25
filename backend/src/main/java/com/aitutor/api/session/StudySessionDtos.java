package com.aitutor.api.session;

import jakarta.validation.constraints.NotNull;
import java.util.List;
import java.util.UUID;

record StartSessionRequest(@NotNull UUID assignmentId) {
}

record StudySessionResponse(
        String id,
        String assignmentId,
        String status,
        int currentStepOrder,
        String startedAt,
        List<ProgressCaptureResponse> progressCaptures
) {
}

record ProgressCaptureResponse(
        String id,
        Integer completionPercent,
        Double confidence,
        Integer behindMinutes,
        String summary
) {
}

record FocusEventRequest(
        String eventType,
        Integer durationSeconds,
        String note
) {
}

record FocusEventResponse(
        String id,
        String eventType,
        Integer durationSeconds,
        String note
) {
}
