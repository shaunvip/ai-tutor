package com.aitutor.api.assignment;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AssignmentPlanStepRepository extends JpaRepository<AssignmentPlanStep, UUID> {

    List<AssignmentPlanStep> findByAssignmentIdOrderByStepOrder(UUID assignmentId);

    void deleteByAssignmentId(UUID assignmentId);
}
