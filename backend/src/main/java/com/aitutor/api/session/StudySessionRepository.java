package com.aitutor.api.session;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StudySessionRepository extends JpaRepository<StudySession, UUID> {

    List<StudySession> findByStudentIdOrderByStartedAtDesc(UUID studentId);
}
