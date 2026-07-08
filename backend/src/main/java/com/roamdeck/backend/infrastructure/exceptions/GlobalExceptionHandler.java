package com.roamdeck.backend.infrastructure.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.NonNull;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(EmailAlreadyExistsException.class)
    public ResponseEntity<ApiErrorResponse> handleEmailAlreadyExists(
            EmailAlreadyExistsException ex
    ) {
        return buildErrorResponse(HttpStatus.CONFLICT, ex.getMessage());
    }

    @ExceptionHandler(ItineraryGenerationException.class)
    public ResponseEntity<ApiErrorResponse> handleItineraryGeneration(
            ItineraryGenerationException ex
    ) {
        return buildErrorResponse(HttpStatus.BAD_GATEWAY, ex.getMessage());
    }

    private ResponseEntity<ApiErrorResponse> buildErrorResponse(@NonNull HttpStatus status, String message) {
        return ResponseEntity
                .status(status)
                .body(new ApiErrorResponse(Instant.now(), status.value(), message));
    }

}