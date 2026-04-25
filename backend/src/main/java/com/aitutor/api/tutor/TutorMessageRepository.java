package com.aitutor.api.tutor;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TutorMessageRepository extends JpaRepository<TutorMessage, UUID> {

    List<TutorMessage> findByThreadIdOrderByCreatedAt(UUID threadId);
}
