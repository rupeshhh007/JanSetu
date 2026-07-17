package com.jansetu.backend.auth.service;

import com.jansetu.backend.auth.dto.LoginRequest;
import com.jansetu.backend.auth.dto.LoginResponse;
import com.jansetu.backend.auth.dto.RegisterRequest;
import com.jansetu.backend.auth.dto.RegisterResponse;
import com.jansetu.backend.auth.entity.Role;
import com.jansetu.backend.auth.entity.User;
import com.jansetu.backend.auth.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService; // NEW: Bring in the badge printer

    @Value("${application.security.jwt.expiration}")
    private long jwtExpiration; // NEW: We need this for the response DTO

    // Constructor Injection updated
    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtService jwtService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    public RegisterResponse register(RegisterRequest request) {
        Optional<User> existingUser = userRepository.findByEmail(request.email());
        if (existingUser.isPresent()) {
            throw new RuntimeException("Email is already registered");
        }

        String hashedPassword = passwordEncoder.encode(request.password());

        User user = new User();
        user.setEmail(request.email());
        user.setPasswordHash(hashedPassword);
        user.setRole(Role.CITIZEN);
        user.setEnabled(true);

        User savedUser = userRepository.save(user);

        return new RegisterResponse(savedUser.getId(), "User registered successfully");
    }

    // NEW: The Login Logic
    public LoginResponse login(LoginRequest request) {

        // 1. Find the user by email
        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new RuntimeException("Invalid credentials"));

        // 2. Check if the password matches the hash in the database
        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new RuntimeException("Invalid credentials");
        }

        // 3. Generate the JWT
        String jwtToken = jwtService.generateToken(user.getEmail());

        // 4. Return the VIP Badge
        return new LoginResponse(jwtToken, "Bearer", jwtExpiration);
    }
}