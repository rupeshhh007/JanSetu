package com.jansetu.backend.profile.fact;

import com.jansetu.backend.common.exception.BusinessException;

public class InvalidProfileFactException extends BusinessException {
    public InvalidProfileFactException(String message) {
        super(message);
    }
}
