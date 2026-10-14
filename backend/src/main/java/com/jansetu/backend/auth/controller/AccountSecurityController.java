package com.jansetu.backend.auth.controller;

import com.jansetu.backend.auth.dto.ChangePasswordRequest;
import com.jansetu.backend.auth.service.AccountSecurityService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

/** Authenticated self-service control; never accepts a target user ID. */
@RestController
@RequestMapping("/api/v1/citizen/account")
public class AccountSecurityController {
    private final AccountSecurityService accounts;

    public AccountSecurityController(AccountSecurityService accounts) {
        this.accounts = accounts;
    }

    @PostMapping("/password")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void changePassword(@Valid @RequestBody ChangePasswordRequest request) {
        accounts.changePassword(request);
    }
}
