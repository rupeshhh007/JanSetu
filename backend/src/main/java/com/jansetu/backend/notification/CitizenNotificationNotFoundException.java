package com.jansetu.backend.notification;

import com.jansetu.backend.common.exception.BusinessException;

public class CitizenNotificationNotFoundException extends BusinessException {
    public CitizenNotificationNotFoundException(String message) { super(message); }
}
