package com.roamdeck.backend.infrastructure.exceptions;

import java.time.Instant;

public record ApiErrorResponse(
    Instant timestamp,
    int status,
    String message
) {
}