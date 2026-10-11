package com.jansetu.backend.application.exception;

import com.jansetu.backend.common.exception.BusinessException;

public class InvalidApplicationStatusTransitionException extends BusinessException {
    public InvalidApplicationStatusTransitionException(String message) {
        super(message);
    }
}
