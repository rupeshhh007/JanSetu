package com.jansetu.backend.auth.service;

import com.jansetu.backend.auth.dto.LoginRequest;
import com.jansetu.backend.auth.dto.LoginResponse;
import com.jansetu.backend.auth.dto.RegisterRequest;
import com.jansetu.backend.auth.dto.RegisterResponse;
import com.jansetu.backend.auth.entity.User;
import com.jansetu.backend.auth.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import com.jansetu.backend.auth.exception.EmailAlreadyExistsException;
import com.jansetu.backend.auth.exception.InvalidCredentialsException;
import java.util.Optional;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final String nonexistentUserHash;

    @Value("${application.security.jwt.expiration}")
    private long jwtExpiration;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtService jwtService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.nonexistentUserHash = passwordEncoder.encode("jansetu-unregistered-account-sentinel");
    }

    public RegisterResponse register(RegisterRequest request) {
        String email = request.email().trim().toLowerCase(java.util.Locale.ROOT);

        Optional<User> existingUser = userRepository.findByEmail(email);
        if (existingUser.isPresent()) {

            throw new EmailAlreadyExistsException();
        }

        String hashedPassword = passwordEncoder.encode(request.password());

        User user = new User();
        user.setEmail(email);
        user.setPasswordHash(hashedPassword);
        // Registration is deliberately citizen-only. Administrator accounts are
        // provisioned by an operator, never inferred from an email address.
        user.setEnabled(true);

        User savedUser = userRepository.save(user);

        return new RegisterResponse(savedUser.getId(), savedUser.getEmail(), "User registered successfully", savedUser.getRole().name());
    }

    public LoginResponse login(LoginRequest request) {
        String email = request.email().trim().toLowerCase(java.util.Locale.ROOT);

        // Verify a BCrypt hash for both existing and unknown accounts, reducing
        // username-enumeration timing differences. The sentinel is not a login.
        User user = userRepository.findByEmail(email).orElse(null);
        String storedHash = user == null ? nonexistentUserHash : user.getPasswordHash();
        boolean passwordMatches = passwordEncoder.matches(request.password(), storedHash);
        if (user == null || !user.isEnabled() || !passwordMatches) {
            throw new InvalidCredentialsException();
        }

        // 3. Generate the JWT
        String jwtToken = jwtService.generateToken(user.getEmail());

        // 4. Return the VIP Badge
        return new LoginResponse(jwtToken, "Bearer", jwtExpiration, user.getRole().name());
    }
}
