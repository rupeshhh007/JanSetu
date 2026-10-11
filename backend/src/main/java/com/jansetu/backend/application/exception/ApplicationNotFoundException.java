package com.jansetu.backend.application.exception;

import com.jansetu.backend.common.exception.BusinessException;

public class ApplicationNotFoundException extends BusinessException {
    public ApplicationNotFoundException(String message) {
        super(message);
    }
}
