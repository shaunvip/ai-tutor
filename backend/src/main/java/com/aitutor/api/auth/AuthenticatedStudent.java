package com.aitutor.api.auth;

import java.util.UUID;

public record AuthenticatedStudent(UUID id, String username) {
}
