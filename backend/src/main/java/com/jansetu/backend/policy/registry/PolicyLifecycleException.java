package com.jansetu.backend.policy.registry;

import com.jansetu.backend.common.exception.BusinessException;

public class PolicyLifecycleException extends BusinessException {
    public PolicyLifecycleException(String message) {
        super(message);
    }
}
