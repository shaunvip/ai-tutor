package com.aitutor.api.auth;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StudentSessionTokenRepository extends JpaRepository<StudentSessionToken, UUID> {

    Optional<StudentSessionToken> findByToken(String token);
}
