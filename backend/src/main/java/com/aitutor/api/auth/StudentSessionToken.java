package com.aitutor.api.auth;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "student_session_tokens")
public class StudentSessionToken {

    @Id
    private UUID id;

    @Column(nullable = false, unique = true)
    private String token;

    @Column(nullable = false)
    private UUID studentId;

    @Column(nullable = false)
    private Instant createdAt;

    protected StudentSessionToken() {
    }

    public StudentSessionToken(String token, UUID studentId) {
        this.id = UUID.randomUUID();
        this.token = token;
        this.studentId = studentId;
        this.createdAt = Instant.now();
    }

    public String getToken() {
        return token;
    }

    public UUID getStudentId() {
        return studentId;
    }
}
