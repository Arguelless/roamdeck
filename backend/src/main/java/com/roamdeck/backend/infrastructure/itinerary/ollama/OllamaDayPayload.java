package com.roamdeck.backend.infrastructure.itinerary;

public record OllamaDayPayload(
    String date,
    java.util.List<OllamaActivityPayload> activities
) {
}