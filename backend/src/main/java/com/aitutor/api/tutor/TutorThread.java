package com.aitutor.api.tutor;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "tutor_threads")
public class TutorThread {

    @Id
    private UUID id;

    @Column(nullable = false)
    private UUID studentId;

    private UUID sessionId;

    @Column(nullable = false)
    private Instant createdAt;

    protected TutorThread() {
    }

    public TutorThread(UUID studentId, UUID sessionId) {
        this.id = UUID.randomUUID();
        this.studentId = studentId;
        this.sessionId = sessionId;
        this.createdAt = Instant.now();
    }

    public UUID getId() {
        return id;
    }

    public UUID getStudentId() {
        return studentId;
    }

    public UUID getSessionId() {
        return sessionId;
    }
}
