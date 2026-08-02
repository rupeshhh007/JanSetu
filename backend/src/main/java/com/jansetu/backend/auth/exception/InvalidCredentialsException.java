package com.jansetu.backend.auth.exception;

import com.jansetu.backend.common.exception.BusinessException;

public class InvalidCredentialsException extends BusinessException {

    public InvalidCredentialsException() {
        super("Invalid email or password.");
    }
}