package com.aitutor.api.session;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "progress_captures")
public class ProgressCapture {

    @Id
    private UUID id;

    @Column(nullable = false)
    private UUID sessionId;

    @Column(nullable = false)
    private String objectKey;

    @Column(nullable = false)
    private String filePath;

    private Integer completionPercent;
    private Double confidence;
    private Integer behindMinutes;

    @Column(length = 2000)
    private String summary;

    @Column(nullable = false)
    private Instant createdAt;

    protected ProgressCapture() {
    }

    public ProgressCapture(UUID sessionId, String objectKey, String filePath) {
        this.id = UUID.randomUUID();
        this.sessionId = sessionId;
        this.objectKey = objectKey;
        this.filePath = filePath;
        this.createdAt = Instant.now();
    }

    public void applyAnalysis(int completionPercent, double confidence, int behindMinutes, String summary) {
        this.completionPercent = completionPercent;
        this.confidence = confidence;
        this.behindMinutes = behindMinutes;
        this.summary = summary;
    }

    public UUID getId() {
        return id;
    }

    public String getFilePath() {
        return filePath;
    }

    public Integer getCompletionPercent() {
        return completionPercent;
    }

    public Double getConfidence() {
        return confidence;
    }

    public Integer getBehindMinutes() {
        return behindMinutes;
    }

    public String getSummary() {
        return summary;
    }
}
