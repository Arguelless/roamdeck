package com.roamdeck.backend.infrastructure.itinerary.ollama;

record OllamaDayPayload(
    String date,
    java.util.List<OllamaActivityPayload> activities
) {
}