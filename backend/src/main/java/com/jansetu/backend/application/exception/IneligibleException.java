package com.jansetu.backend.application.exception;

import com.jansetu.backend.common.exception.BusinessException;

public class IneligibleException extends BusinessException {

    public IneligibleException(String message) {
        super(message);
    }

}