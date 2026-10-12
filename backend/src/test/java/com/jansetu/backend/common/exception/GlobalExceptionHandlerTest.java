package com.jansetu.backend.common.exception;

import com.jansetu.backend.profile.fact.InvalidProfileFactException;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import static org.junit.jupiter.api.Assertions.assertEquals;

/** Regression: invalid progressive facts must not reach the generic 500 handler. */
class GlobalExceptionHandlerTest {

    @Test
    void returnsBadRequestForInvalidProfileFact() {
        var response = new GlobalExceptionHandler().handleBadRequest(
                new InvalidProfileFactException("A fact expiry must be in the future."));

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("A fact expiry must be in the future.", response.getBody().getMessage());
    }
}
