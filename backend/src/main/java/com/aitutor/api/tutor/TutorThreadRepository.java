package com.aitutor.api.tutor;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TutorThreadRepository extends JpaRepository<TutorThread, UUID> {

    List<TutorThread> findByStudentIdOrderByCreatedAtDesc(UUID studentId);
}
