package com.jansetu.backend.auth.service;

import com.jansetu.backend.auth.entity.Role;
import com.jansetu.backend.auth.entity.User;
import com.jansetu.backend.auth.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Locale;

@Slf4j
@Component
public class AdminBootstrapRunner implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${application.security.bootstrap-admin.email:admin@jansetu.gov.in}")
    private String adminEmail;

    @Value("${application.security.bootstrap-admin.password:}")
    private String adminPassword;

    public AdminBootstrapRunner(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        if (adminPassword == null || adminPassword.isBlank()) {
            log.info("Administrator bootstrap is disabled; configure JANSETU_ADMIN_PASSWORD to enable it.");
            return;
        }
        if (adminPassword.length() < 12) {
            throw new IllegalStateException("Bootstrap administrator password must have at least 12 characters.");
        }
        if (userRepository.existsByRole(Role.ADMIN)) {
            log.info("Administrator account already present. Skipping admin bootstrap.");
            return;
        }

        String normalizedEmail = adminEmail.trim().toLowerCase(Locale.ROOT);
        if (userRepository.findByEmail(normalizedEmail).isPresent()) {
            log.warn("User with bootstrap email {} already exists but is not an administrator.", normalizedEmail);
            return;
        }

        User adminUser = new User();
        adminUser.setEmail(normalizedEmail);
        adminUser.setPasswordHash(passwordEncoder.encode(adminPassword));
        adminUser.setRole(Role.ADMIN);
        adminUser.setEnabled(true);

        userRepository.save(adminUser);
        log.info("Successfully bootstrapped initial administrator account: {}", normalizedEmail);
    }
}
