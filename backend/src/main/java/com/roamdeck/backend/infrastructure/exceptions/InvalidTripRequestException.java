package com.roamdeck.backend.infrastructure.exceptions;

public class InvalidTripRequestException extends RuntimeException {

    public InvalidTripRequestException(String message) {
        super(message);
    }
}
