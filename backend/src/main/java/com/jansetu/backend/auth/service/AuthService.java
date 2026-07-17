package com.jansetu.backend.auth.service;

import com.jansetu.backend.auth.dto.RegisterRequest;
import com.jansetu.backend.auth.dto.RegisterResponse;
import com.jansetu.backend.auth.entity.Role;
import com.jansetu.backend.auth.entity.User;
import com.jansetu.backend.auth.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    // Constructor Injection
    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public RegisterResponse register(RegisterRequest request) {

        // 1. Check if user already exists
        Optional<User> existingUser = userRepository.findByEmail(request.email());
        if (existingUser.isPresent()) {
            throw new RuntimeException("Email is already registered");
        }

        // 2. Hash the password
        String hashedPassword = passwordEncoder.encode(request.password());

        // 3. Create the User entity
        User user = new User();
        user.setEmail(request.email());
        user.setPasswordHash(hashedPassword);
        user.setRole(Role.CITIZEN);
        user.setEnabled(true);

        // 4. Save to database
        User savedUser = userRepository.save(user);

        // 5. Return the DTO
        return new RegisterResponse(savedUser.getId(), "User registered successfully");
    }
}