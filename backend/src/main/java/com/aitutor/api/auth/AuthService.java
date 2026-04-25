package com.aitutor.api.auth;

import java.security.SecureRandom;
import java.util.Base64;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private final StudentRepository students;
    private final StudentSessionTokenRepository tokens;
    private final PasswordEncoder passwordEncoder;
    private final SecureRandom secureRandom = new SecureRandom();

    public AuthService(StudentRepository students, StudentSessionTokenRepository tokens, PasswordEncoder passwordEncoder) {
        this.students = students;
        this.tokens = tokens;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (students.existsByUsername(request.username())) {
            throw new IllegalArgumentException("Username is already taken");
        }

        Student student = students.save(new Student(
                request.username(),
                passwordEncoder.encode(request.password()),
                request.displayName(),
                request.age(),
                request.gradeLevel(),
                request.language()
        ));

        return issueToken(student);
    }

    @Transactional
    public AuthResponse login(LoginRequest request) {
        Student student = students.findByUsername(request.username())
                .orElseThrow(() -> new IllegalArgumentException("Invalid username or password"));

        if (!passwordEncoder.matches(request.password(), student.getPasswordHash())) {
            throw new IllegalArgumentException("Invalid username or password");
        }

        return issueToken(student);
    }

    private AuthResponse issueToken(Student student) {
        byte[] tokenBytes = new byte[32];
        secureRandom.nextBytes(tokenBytes);
        String token = Base64.getUrlEncoder().withoutPadding().encodeToString(tokenBytes);
        tokens.save(new StudentSessionToken(token, student.getId()));

        return new AuthResponse(
                token,
                new StudentResponse(
                        student.getId().toString(),
                        student.getUsername(),
                        student.getDisplayName(),
                        student.getAge(),
                        student.getGradeLevel(),
                        student.getLanguage()
                )
        );
    }
}
