package com.aitutor.api.tutor;

import java.util.UUID;

record CreateTutorThreadRequest(UUID sessionId) {
}

record SendTutorMessageRequest(String mode, String content) {
}

record TutorThreadResponse(String id, String sessionId) {
}

record TutorMessageResponse(String id, String studentRole, String tutorRole, String content) {
}
