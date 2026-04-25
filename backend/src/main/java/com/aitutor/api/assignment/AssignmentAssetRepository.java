package com.aitutor.api.assignment;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AssignmentAssetRepository extends JpaRepository<AssignmentAsset, UUID> {

    List<AssignmentAsset> findByAssignmentId(UUID assignmentId);

    Optional<AssignmentAsset> findFirstByAssignmentIdAndAssetTypeOrderByCreatedAtDesc(UUID assignmentId, String assetType);
}
