package com.jansetu.backend.auth.exception;

import com.jansetu.backend.common.exception.BusinessException;

public class EmailAlreadyExistsException extends BusinessException {

    public EmailAlreadyExistsException() {
        super("Email is already registered.");
    }
}