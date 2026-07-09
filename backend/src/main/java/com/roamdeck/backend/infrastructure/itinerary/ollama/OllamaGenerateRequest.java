package com.roamdeck.backend.infrastructure.itinerary.ollama;

import com.fasterxml.jackson.databind.JsonNode;

record OllamaGenerateRequest(
    String model,
    String prompt,
    JsonNode format,
    OllamaOptions options,
    boolean think,
    boolean stream
) {
}