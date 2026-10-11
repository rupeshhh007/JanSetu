package com.jansetu.backend.source;

import com.jansetu.backend.common.exception.BusinessException;

public class OfficialSourceNotFoundException extends BusinessException {
    public OfficialSourceNotFoundException(String message) { super(message); }
}
