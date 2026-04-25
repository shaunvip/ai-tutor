package com.aitutor.api.storage;

public record StoredFile(String objectKey, String filePath, String contentType, long sizeBytes) {
}
