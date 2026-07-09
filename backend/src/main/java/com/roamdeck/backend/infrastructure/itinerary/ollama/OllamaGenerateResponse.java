package com.roamdeck.backend.infrastructure.itinerary.ollama;

record OllamaGenerateResponse(
    String model,
    String response,
    boolean done
) {
}