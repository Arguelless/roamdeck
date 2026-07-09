package com.roamdeck.backend.infrastructure.itinerary;

import com.fasterxml.jackson.databind.JsonNode;

public record OllamaGenerateRequest(
    String model,
    String prompt,
    JsonNode format,
    OllamaOptions options,
    boolean think,
    boolean stream
) {
}