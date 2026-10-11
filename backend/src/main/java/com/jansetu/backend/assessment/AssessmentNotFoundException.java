package com.jansetu.backend.assessment;

import com.jansetu.backend.common.exception.BusinessException;

public class AssessmentNotFoundException extends BusinessException {
    public AssessmentNotFoundException(String message) {
        super(message);
    }
}
