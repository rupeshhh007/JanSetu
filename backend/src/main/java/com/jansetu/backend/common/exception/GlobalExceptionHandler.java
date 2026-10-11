package com.jansetu.backend.common.exception;

import com.jansetu.backend.application.exception.*;
import com.jansetu.backend.common.dto.ErrorResponse;
import com.jansetu.backend.profile.exception.ProfileNotFoundException;
import com.jansetu.backend.scheme.exception.SchemeNotFoundException;
import com.jansetu.backend.scheme.exception.SchemePublicationException;
import com.jansetu.backend.policy.registry.PolicyLifecycleException;
import com.jansetu.backend.policy.registry.PolicyVersionNotFoundException;
import com.jansetu.backend.policy.validation.InvalidPolicyException;
import com.jansetu.backend.assessment.AssessmentNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import com.jansetu.backend.auth.exception.EmailAlreadyExistsException;
import com.jansetu.backend.auth.exception.InvalidCredentialsException;
import java.time.LocalDateTime;
import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(InvalidCredentialsException.class)
    public ResponseEntity<ErrorResponse> handleInvalidCredentials(
            InvalidCredentialsException ex) {

        return buildResponse(
                HttpStatus.UNAUTHORIZED,
                "Unauthorized",
                ex.getMessage()
        );
    }

    @ExceptionHandler(EmailAlreadyExistsException.class)
    public ResponseEntity<ErrorResponse> handleEmailAlreadyExists(
            EmailAlreadyExistsException ex) {

        return buildResponse(
                HttpStatus.CONFLICT,
                "Conflict",
                ex.getMessage()
        );
    }

    // Handle Not Found exceptions (404)
    @ExceptionHandler({ProfileNotFoundException.class, SchemeNotFoundException.class, ApplicationNotFoundException.class,
            PolicyVersionNotFoundException.class, AssessmentNotFoundException.class})
    public ResponseEntity<ErrorResponse> handleNotFound(BusinessException ex) {
        return buildResponse(HttpStatus.NOT_FOUND, "Resource Not Found", ex.getMessage());
    }

    // Handle Conflict exceptions (409)
    @ExceptionHandler({DuplicateApplicationException.class, InvalidApplicationStatusTransitionException.class,
            PolicyLifecycleException.class})
    public ResponseEntity<ErrorResponse> handleConflict(BusinessException ex) {
        return buildResponse(HttpStatus.CONFLICT, "Conflict", ex.getMessage());
    }

    // Handle Bad Request exceptions (400)
    @ExceptionHandler(IneligibleException.class)
    public ResponseEntity<ErrorResponse> handleBadRequest(IneligibleException ex) {
        return buildResponse(HttpStatus.BAD_REQUEST, "Bad Request", ex.getMessage());
    }

    @ExceptionHandler(SchemePublicationException.class)
    public ResponseEntity<ErrorResponse> handleSchemePublication(SchemePublicationException ex) {
        return buildResponse(HttpStatus.BAD_REQUEST, "Scheme Is Not Ready To Publish", ex.getMessage());
    }

    @ExceptionHandler(InvalidPolicyException.class)
    public ResponseEntity<ErrorResponse> handleInvalidPolicy(InvalidPolicyException ex) {
        return buildResponse(HttpStatus.BAD_REQUEST, "Invalid Policy", ex.getMessage());
    }

    // Handle Validation Errors (400)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationExceptions(MethodArgumentNotValidException ex) {
        // Collect all validation error messages into a single comma-separated string
        String errorMessage = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.joining(", "));

        return buildResponse(HttpStatus.BAD_REQUEST, "Validation Failed", errorMessage);
    }

    @ExceptionHandler({org.springframework.http.converter.HttpMessageNotReadableException.class,
            org.springframework.web.method.annotation.MethodArgumentTypeMismatchException.class})
    public ResponseEntity<ErrorResponse> handleMalformedRequest(Exception ex) {
        return buildResponse(HttpStatus.BAD_REQUEST, "Invalid Request", "Check the request format and field types.");
    }

    @ExceptionHandler(org.springframework.orm.ObjectOptimisticLockingFailureException.class)
    public ResponseEntity<ErrorResponse> handleConcurrentUpdate(Exception ex) {
        return buildResponse(HttpStatus.CONFLICT, "Concurrent Update", "This record changed. Refresh and try again.");
    }

    // Fallback for unexpected exceptions (500)
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(Exception ex) {
        // This prints the error in your console so you can actually debug it
        log.error("An unexpected error occurred: ", ex);
        return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, "Internal Server Error", "An unexpected error occurred. Please try again later.");
    }

    // Helper method to keep code clean
    private ResponseEntity<ErrorResponse> buildResponse(HttpStatus status, String error, String message) {
        ErrorResponse response = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(status.value())
                .error(error)
                .message(message)
                .build();
        return new ResponseEntity<>(response, status);
    }
}
