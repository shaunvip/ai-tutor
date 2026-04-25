package com.aitutor.api.session;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "study_sessions")
public class StudySession {

    @Id
    private UUID id;

    @Column(nullable = false)
    private UUID studentId;

    @Column(nullable = false)
    private UUID assignmentId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StudySessionStatus status;

    @Column(nullable = false)
    private int currentStepOrder;

    @Column(nullable = false)
    private Instant startedAt;

    private Instant endedAt;

    protected StudySession() {
    }

    public StudySession(UUID studentId, UUID assignmentId) {
        this.id = UUID.randomUUID();
        this.studentId = studentId;
        this.assignmentId = assignmentId;
        this.status = StudySessionStatus.ACTIVE;
        this.currentStepOrder = 1;
        this.startedAt = Instant.now();
    }

    public void markStepComplete(int stepOrder) {
        this.currentStepOrder = Math.max(this.currentStepOrder, stepOrder + 1);
    }

    public void complete() {
        this.status = StudySessionStatus.COMPLETED;
        this.endedAt = Instant.now();
    }

    public UUID getId() {
        return id;
    }

    public UUID getStudentId() {
        return studentId;
    }

    public UUID getAssignmentId() {
        return assignmentId;
    }

    public StudySessionStatus getStatus() {
        return status;
    }

    public int getCurrentStepOrder() {
        return currentStepOrder;
    }

    public Instant getStartedAt() {
        return startedAt;
    }
}
