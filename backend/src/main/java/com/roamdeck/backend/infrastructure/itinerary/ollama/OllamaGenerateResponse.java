package com.roamdeck.backend.infrastructure.itinerary;

public record OllamaGenerateResponse(
    String model,
    String response,
    boolean done
) {
}