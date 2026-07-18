package com.jansetu.backend.profile.exception;

import com.jansetu.backend.common.exception.BusinessException;

public class ProfileNotFoundException extends BusinessException {

    public ProfileNotFoundException(String message) {
        super(message);
    }

}