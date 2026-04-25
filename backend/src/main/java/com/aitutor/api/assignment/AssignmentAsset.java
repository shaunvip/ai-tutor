package com.aitutor.api.assignment;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "assignment_assets")
public class AssignmentAsset {

    @Id
    private UUID id;

    @Column(nullable = false)
    private UUID assignmentId;

    @Column(nullable = false)
    private String assetType;

    @Column(nullable = false)
    private String objectKey;

    @Column(nullable = false)
    private String filePath;

    private String contentType;

    @Column(nullable = false)
    private long sizeBytes;

    @Column(nullable = false)
    private Instant createdAt;

    protected AssignmentAsset() {
    }

    public AssignmentAsset(UUID assignmentId, String assetType, String objectKey, String filePath,
                           String contentType, long sizeBytes) {
        this.id = UUID.randomUUID();
        this.assignmentId = assignmentId;
        this.assetType = assetType;
        this.objectKey = objectKey;
        this.filePath = filePath;
        this.contentType = contentType;
        this.sizeBytes = sizeBytes;
        this.createdAt = Instant.now();
    }

    public UUID getId() {
        return id;
    }

    public UUID getAssignmentId() {
        return assignmentId;
    }

    public String getAssetType() {
        return assetType;
    }

    public String getObjectKey() {
        return objectKey;
    }

    public String getFilePath() {
        return filePath;
    }
}
