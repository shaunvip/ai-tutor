package com.aitutor.api.config;

import java.time.Duration;
import java.util.Set;

public final class AppConstants {

    public static final Set<String> SUBJECTS = Set.of("HINDI", "ENGLISH", "MATHS");

    public static final String ASSET_TYPE_HOMEWORK_IMAGE = "HOMEWORK_IMAGE";

    public static final String STORAGE_CATEGORY_ASSIGNMENTS = "assignments";
    public static final String STORAGE_CATEGORY_PROGRESS = "progress";
    public static final String STORAGE_CATEGORY_FOCUS = "focus";

    public static final String SESSION_EVENT_STARTED = "SESSION_STARTED";
    public static final String SESSION_EVENT_STEP_COMPLETED = "STEP_COMPLETED";
    public static final String SESSION_EVENT_COMPLETED = "SESSION_COMPLETED";
    public static final String SESSION_EVENT_FOCUS = "FOCUS_EVENT";
    public static final String SESSION_EVENT_PROGRESS_CAPTURE_ANALYZED = "PROGRESS_CAPTURE_ANALYZED";
    public static final String SESSION_EVENT_LOOKING_AWAY_ALERT = "LOOKING_AWAY_ALERT";

    public static final String AUTHORIZATION_HEADER = "Authorization";
    public static final String BEARER_PREFIX = "Bearer ";
    public static final String WORKER_TOKEN_HEADER = "X-AI-Tutor-Worker-Token";

    public static final String WORKER_PATH_ANALYZE_ASSIGNMENT = "/internal/analyze-assignment";
    public static final String WORKER_PATH_ANALYZE_PROGRESS = "/internal/analyze-progress";
    public static final String WORKER_PATH_ANALYZE_FOCUS = "/internal/analyze-focus";
    public static final String WORKER_PATH_TUTOR_HINT = "/internal/tutor-hint";

    public static final int API_REQUEST_LOG_MAX_BODY_CHARS = 2_000;
    public static final int WORKER_LOG_MAX_BODY_CHARS = 2_000;

    public static final Duration WORKER_CONNECT_TIMEOUT = Duration.ofMinutes(5);
    public static final Duration WORKER_READ_TIMEOUT = Duration.ofMinutes(5);

    private AppConstants() {
    }
}
