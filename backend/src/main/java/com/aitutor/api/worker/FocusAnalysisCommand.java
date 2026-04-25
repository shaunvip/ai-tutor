package com.aitutor.api.worker;

public record FocusAnalysisCommand(
        String sessionId,
        String focusAssetPath
) {
}
