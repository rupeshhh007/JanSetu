package com.jansetu.backend.application.exception;

import com.jansetu.backend.common.exception.BusinessException;

public class DuplicateApplicationException extends BusinessException {

    public DuplicateApplicationException(String message) {
        super(message);
    }

}