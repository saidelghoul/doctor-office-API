package com.clinic.doctor_office.config;

import com.clinic.doctor_office.domain.Role;
import com.clinic.doctor_office.domain.User;
import com.clinic.doctor_office.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * ############################  DEV ONLY  ############################
 * Creates one doctor and one assistant at startup, with well-known passwords.
 * It only exists when app.seed.enabled=true (set in application.properties for
 * development, absent/false everywhere else). It never overwrites an existing user.
 * Remove it, or keep the switch off, before any real deployment.
 * #####################################################################
 */
@Slf4j
@Component
@ConditionalOnProperty(name = "app.seed.enabled", havingValue = "true")
@RequiredArgsConstructor
public class DevDataSeeder implements CommandLineRunner {

    public static final String DOCTOR_USERNAME = "doctor";
    public static final String DOCTOR_PASSWORD = "doctor123";
    public static final String ASSISTANT_USERNAME = "assistant";
    public static final String ASSISTANT_PASSWORD = "assistant123";

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        log.warn("DEV ONLY: seeding users '{}' and '{}' with well-known passwords", DOCTOR_USERNAME, ASSISTANT_USERNAME);
        createIfMissing(DOCTOR_USERNAME, DOCTOR_PASSWORD, "Dr. Dev", Role.DOCTOR);
        createIfMissing(ASSISTANT_USERNAME, ASSISTANT_PASSWORD, "Assistant Dev", Role.ASSISTANT);
    }

    private void createIfMissing(String username, String rawPassword, String fullName, Role role) {
        if (userRepository.findByUsername(username).isPresent()) {
            return;
        }
        userRepository.save(User.builder()
                .username(username)
                .passwordHash(passwordEncoder.encode(rawPassword)) // BCrypt hash, never the raw password
                .fullName(fullName)
                .role(role)
                .enabled(true)
                .build());
    }
}
