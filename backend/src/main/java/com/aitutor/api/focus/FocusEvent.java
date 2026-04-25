package com.aitutor.api.focus;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "focus_events")
public class FocusEvent {

    @Id
    private UUID id;

    @Column(nullable = false)
    private UUID sessionId;

    @Column(nullable = false)
    private String eventType;

    private Integer durationSeconds;

    @Column(length = 1000)
    private String note;

    @Column(nullable = false)
    private Instant createdAt;

    protected FocusEvent() {
    }

    public FocusEvent(UUID sessionId, String eventType, Integer durationSeconds, String note) {
        this.id = UUID.randomUUID();
        this.sessionId = sessionId;
        this.eventType = eventType;
        this.durationSeconds = durationSeconds;
        this.note = note;
        this.createdAt = Instant.now();
    }

    public UUID getId() {
        return id;
    }
}
