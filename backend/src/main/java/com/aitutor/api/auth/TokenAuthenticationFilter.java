package com.aitutor.api.auth;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class TokenAuthenticationFilter extends OncePerRequestFilter {

    private final StudentSessionTokenRepository tokens;
    private final StudentRepository students;

    public TokenAuthenticationFilter(StudentSessionTokenRepository tokens, StudentRepository students) {
        this.tokens = tokens;
        this.students = students;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String header = request.getHeader("Authorization");
        if (header != null && header.startsWith("Bearer ")) {
            String tokenValue = header.substring("Bearer ".length()).trim();
            tokens.findByToken(tokenValue)
                    .flatMap(token -> students.findById(token.getStudentId()))
                    .ifPresent(student -> {
                        AuthenticatedStudent principal = new AuthenticatedStudent(student.getId(), student.getUsername());
                        UsernamePasswordAuthenticationToken authentication =
                                new UsernamePasswordAuthenticationToken(principal, tokenValue, List.of());
                        SecurityContextHolder.getContext().setAuthentication(authentication);
                    });
        }

        filterChain.doFilter(request, response);
    }
}
