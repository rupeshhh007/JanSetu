package com.jansetu.backend.policy.registry;

import com.jansetu.backend.common.exception.BusinessException;

public class PolicyVersionNotFoundException extends BusinessException {
    public PolicyVersionNotFoundException(String message) {
        super(message);
    }
}
