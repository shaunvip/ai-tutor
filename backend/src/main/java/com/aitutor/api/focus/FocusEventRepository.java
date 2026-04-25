package com.aitutor.api.focus;

import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FocusEventRepository extends JpaRepository<FocusEvent, UUID> {
}
