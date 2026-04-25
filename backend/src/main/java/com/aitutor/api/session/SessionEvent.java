package com.aitutor.api.session;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "session_events")
public class SessionEvent {

    @Id
    private UUID id;

    @Column(nullable = false)
    private UUID sessionId;

    @Column(nullable = false)
    private String eventType;

    @Column(length = 4000)
    private String payloadJson;

    @Column(nullable = false)
    private Instant createdAt;

    protected SessionEvent() {
    }

    public SessionEvent(UUID sessionId, String eventType, String payloadJson) {
        this.id = UUID.randomUUID();
        this.sessionId = sessionId;
        this.eventType = eventType;
        this.payloadJson = payloadJson;
        this.createdAt = Instant.now();
    }
}
