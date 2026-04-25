package com.aitutor.api.assignment;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "assignments")
public class Assignment {

    @Id
    private UUID id;

    @Column(nullable = false)
    private UUID studentId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AssignmentStatus status;

    private String subject;
    private String taskType;
    private Integer estimatedWordCount;
    private Integer questionCount;
    private Integer estimatedTotalMinutes;
    private Double confidence;

    @Column(length = 2000)
    private String summary;

    @Column(nullable = false)
    private Instant createdAt;

    protected Assignment() {
    }

    public Assignment(UUID studentId, String subject) {
        this.id = UUID.randomUUID();
        this.studentId = studentId;
        this.subject = subject;
        this.status = AssignmentStatus.CREATED;
        this.createdAt = Instant.now();
    }

    public void markImageUploaded() {
        this.status = AssignmentStatus.IMAGE_UPLOADED;
    }

    public void applyAnalysis(String subject, String taskType, int estimatedWordCount, int questionCount,
                              int estimatedTotalMinutes, double confidence, String summary) {
        this.subject = subject;
        this.taskType = taskType;
        this.estimatedWordCount = estimatedWordCount;
        this.questionCount = questionCount;
        this.estimatedTotalMinutes = estimatedTotalMinutes;
        this.confidence = confidence;
        this.summary = summary;
        this.status = AssignmentStatus.ANALYZED;
    }

    public UUID getId() {
        return id;
    }

    public UUID getStudentId() {
        return studentId;
    }

    public AssignmentStatus getStatus() {
        return status;
    }

    public String getSubject() {
        return subject;
    }

    public String getTaskType() {
        return taskType;
    }

    public Integer getEstimatedWordCount() {
        return estimatedWordCount;
    }

    public Integer getQuestionCount() {
        return questionCount;
    }

    public Integer getEstimatedTotalMinutes() {
        return estimatedTotalMinutes;
    }

    public Double getConfidence() {
        return confidence;
    }

    public String getSummary() {
        return summary;
    }
}
