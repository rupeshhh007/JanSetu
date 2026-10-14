package com.jansetu.backend.auth.exception;

/** Safe, intentionally short client-facing explanations for password changes. */
public final class AccountPasswordException extends RuntimeException {
    public AccountPasswordException(String message) {
        super(message);
    }
}
