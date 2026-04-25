package com.aitutor.api.session;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProgressCaptureRepository extends JpaRepository<ProgressCapture, UUID> {

    List<ProgressCapture> findBySessionIdOrderByCreatedAtDesc(UUID sessionId);
}
