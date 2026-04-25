package com.aitutor.api.tutor;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "tutor_messages")
public class TutorMessage {

    @Id
    private UUID id;

    @Column(nullable = false)
    private UUID threadId;

    @Column(nullable = false)
    private String role;

    @Column(length = 8000, nullable = false)
    private String content;

    @Column(nullable = false)
    private Instant createdAt;

    protected TutorMessage() {
    }

    public TutorMessage(UUID threadId, String role, String content) {
        this.id = UUID.randomUUID();
        this.threadId = threadId;
        this.role = role;
        this.content = content;
        this.createdAt = Instant.now();
    }

    public UUID getId() {
        return id;
    }

    public String getRole() {
        return role;
    }

    public String getContent() {
        return content;
    }
}
