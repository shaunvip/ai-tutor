package com.aitutor.api.common;

import com.aitutor.api.auth.AuthenticatedStudent;
import java.util.UUID;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public class CurrentStudent {

    public UUID id() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof AuthenticatedStudent student)) {
            throw new IllegalStateException("No authenticated student in context");
        }
        return student.id();
    }
}
