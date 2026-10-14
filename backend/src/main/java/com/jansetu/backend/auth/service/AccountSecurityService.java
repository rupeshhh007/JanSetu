package com.jansetu.backend.auth.service;

import com.jansetu.backend.auth.dto.ChangePasswordRequest;
import com.jansetu.backend.auth.entity.User;
import com.jansetu.backend.auth.exception.AccountPasswordException;
import com.jansetu.backend.auth.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Atomically updates password hash and JWT session version.
 * All existing sessions are invalid after the transaction commits.
 */
@Service
public class AccountSecurityService {
    private final CurrentUserService current;
    private final UserRepository users;
    private final PasswordEncoder passwords;

    public AccountSecurityService(CurrentUserService current, UserRepository users, PasswordEncoder passwords) {
        this.current = current;
        this.users = users;
        this.passwords = passwords;
    }

    @Transactional
    public void changePassword(ChangePasswordRequest request) {
        User authenticated = current.getCurrentUser();
        User account = users.lockById(authenticated.getId())
                .orElseThrow(() -> new AccountPasswordException("Account unavailable."));
        if (!passwords.matches(request.currentPassword(), account.getPasswordHash())) {
            throw new AccountPasswordException("Current password is incorrect.");
        }
        if (passwords.matches(request.newPassword(), account.getPasswordHash())) {
            throw new AccountPasswordException("Choose a different password.");
        }
        if (account.getTokenVersion() == Integer.MAX_VALUE) {
            throw new IllegalStateException("Account session version exhausted");
        }

        account.setPasswordHash(passwords.encode(request.newPassword()));
        account.setTokenVersion(account.getTokenVersion() + 1);
        users.saveAndFlush(account);
        // Never log password text, hashes, token contents or JWT signing material.
    }
}
