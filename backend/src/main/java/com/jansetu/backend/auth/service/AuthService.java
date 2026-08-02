package com.jansetu.backend.auth.service;

import com.jansetu.backend.auth.dto.LoginRequest;
import com.jansetu.backend.auth.dto.LoginResponse;
import com.jansetu.backend.auth.dto.RegisterRequest;
import com.jansetu.backend.auth.dto.RegisterResponse;
import com.jansetu.backend.auth.entity.Role;
import com.jansetu.backend.auth.entity.User;
import com.jansetu.backend.auth.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import com.jansetu.backend.auth.exception.EmailAlreadyExistsException;
import com.jansetu.backend.auth.exception.InvalidCredentialsException;
import java.util.Optional;

@Slf4j
@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    @Value("${application.security.jwt.expiration}")
    private long jwtExpiration;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtService jwtService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    public RegisterResponse register(RegisterRequest request) {
        log.info("Attempting to register new user with email: {}", request.email());

        Optional<User> existingUser = userRepository.findByEmail(request.email());
        if (existingUser.isPresent()) {
            log.warn("Registration failed - email already exists: {}", request.email());
            throw new EmailAlreadyExistsException(); // Note: You can swap this to a custom BusinessException later!
        }

        String hashedPassword = passwordEncoder.encode(request.password());

        User user = new User();
        user.setEmail(request.email());
        user.setPasswordHash(hashedPassword);
        user.setRole(Role.CITIZEN);
        user.setEnabled(true);

        User savedUser = userRepository.save(user);

        log.info("Successfully registered user: {}", savedUser.getEmail());
        return new RegisterResponse(savedUser.getId(), "User registered successfully");
    }

    public LoginResponse login(LoginRequest request) {
        log.info("Login attempt for email: {}", request.email());

        // 1. Find the user by email
        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> {
                    log.warn("Login failed - user not found: {}", request.email());
                    return new InvalidCredentialsException();
                });

        // 2. Check if the password matches the hash in the database
        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            log.warn("Login failed - bad password for email: {}", request.email());
            throw new InvalidCredentialsException();
        }

        // 3. Generate the JWT
        String jwtToken = jwtService.generateToken(user.getEmail());

        log.info("User {} successfully logged in", user.getEmail());
        // 4. Return the VIP Badge
        return new LoginResponse(jwtToken, "Bearer", jwtExpiration);
    }
}