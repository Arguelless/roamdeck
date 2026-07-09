package com.roamdeck.backend.infrastructure.itinerary.ollama;

record OllamaActivityPayload(
    String timeOfDay,
    String description
) {
}