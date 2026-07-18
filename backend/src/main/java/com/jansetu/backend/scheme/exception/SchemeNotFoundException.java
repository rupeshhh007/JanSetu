package com.jansetu.backend.scheme.exception;

import com.jansetu.backend.common.exception.BusinessException;

public class SchemeNotFoundException extends BusinessException {

    public SchemeNotFoundException(String message) {
        super(message);
    }

}