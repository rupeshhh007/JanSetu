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
import com.jansetu.backend.notification.CitizenNotificationNotFoundException;
import com.jansetu.backend.source.OfficialSourceNotFoundException;
import com.jansetu.backend.source.SourceIntakeException;
import com.jansetu.backend.profile.fact.InvalidProfileFactException;
import com.jansetu.backend.source.change.SourceChangeReviewException;
import com.jansetu.backend.agent.service.PolicyInvestigationException;
import com.jansetu.backend.agent.service.PolicyInvestigationConflictException;
import com.jansetu.backend.preparation.PreparationException;
import com.jansetu.backend.preparation.PreparationNotFoundException;
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
            PolicyVersionNotFoundException.class, AssessmentNotFoundException.class, OfficialSourceNotFoundException.class,
            CitizenNotificationNotFoundException.class})
    public ResponseEntity<ErrorResponse> handleNotFound(BusinessException ex) {
        return buildResponse(HttpStatus.NOT_FOUND, "Resource Not Found", ex.getMessage());
    }

    @ExceptionHandler(PreparationNotFoundException.class)
    public ResponseEntity<ErrorResponse> handlePreparationNotFound(PreparationNotFoundException ex) {
        return buildResponse(HttpStatus.NOT_FOUND, "Preparation Not Found", ex.getMessage());
    }

    // Handle Conflict exceptions (409)
    @ExceptionHandler({DuplicateApplicationException.class, InvalidApplicationStatusTransitionException.class,
            PolicyLifecycleException.class})
    public ResponseEntity<ErrorResponse> handleConflict(BusinessException ex) {
        return buildResponse(HttpStatus.CONFLICT, "Conflict", ex.getMessage());
    }

    @ExceptionHandler(SourceChangeReviewException.class)
    public ResponseEntity<ErrorResponse> handleSourceChangeReview(SourceChangeReviewException ex) {
        return buildResponse(HttpStatus.CONFLICT, "Source Change Review Conflict", ex.getMessage());
    }

    // Handle Bad Request exceptions (400)
    @ExceptionHandler({IneligibleException.class, InvalidProfileFactException.class})
    public ResponseEntity<ErrorResponse> handleBadRequest(BusinessException ex) {
        return buildResponse(HttpStatus.BAD_REQUEST, "Bad Request", ex.getMessage());
    }

    @ExceptionHandler(SourceIntakeException.class)
    public ResponseEntity<ErrorResponse> handleSourceIntake(SourceIntakeException ex) {
        return buildResponse(HttpStatus.BAD_REQUEST, "Source Intake Rejected", ex.getMessage());
    }

    @ExceptionHandler(PreparationException.class)
    public ResponseEntity<ErrorResponse> handlePreparation(PreparationException ex) {
        return buildResponse(HttpStatus.CONFLICT, "Preparation Conflict", ex.getMessage());
    }

    @ExceptionHandler(PolicyInvestigationException.class)
    public ResponseEntity<ErrorResponse> handlePolicyInvestigation(PolicyInvestigationException ex) {
        return buildResponse(HttpStatus.BAD_REQUEST, "Policy Investigation Rejected", ex.getMessage());
    }

    @ExceptionHandler(PolicyInvestigationConflictException.class)
    public ResponseEntity<ErrorResponse> handlePolicyInvestigationConflict(PolicyInvestigationConflictException ex) {
        return buildResponse(HttpStatus.CONFLICT, "Policy Investigation Conflict", ex.getMessage());
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

    @ExceptionHandler(com.jansetu.backend.auth.security.AuthenticationRateLimitException.class)
    public ResponseEntity<ErrorResponse> handleAuthenticationThrottling(
            com.jansetu.backend.auth.security.AuthenticationRateLimitException ex) {
        return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                .header("Retry-After", Integer.toString(
                        com.jansetu.backend.auth.security.AuthenticationRateLimitException.RETRY_AFTER_SECONDS))
                .header("Cache-Control", "no-store")
                .body(buildResponse(HttpStatus.TOO_MANY_REQUESTS, "Too Many Requests", ex.getMessage()).getBody());
    }

    @ExceptionHandler(org.springframework.web.server.ResponseStatusException.class)
    public ResponseEntity<ErrorResponse> handleExplicitStatus(
            org.springframework.web.server.ResponseStatusException ex) {
        HttpStatus status = HttpStatus.valueOf(ex.getStatusCode().value());
        // Do not echo exception internals or raw database details in public responses.
        return buildResponse(status, status.getReasonPhrase(), "Request cannot be fulfilled.");
    }

    @ExceptionHandler(com.jansetu.backend.auth.exception.AccountPasswordException.class)
    public ResponseEntity<ErrorResponse> handleAccountPassword(
            com.jansetu.backend.auth.exception.AccountPasswordException ex) {
        return buildResponse(HttpStatus.BAD_REQUEST, "Invalid Password Change", ex.getMessage());
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
