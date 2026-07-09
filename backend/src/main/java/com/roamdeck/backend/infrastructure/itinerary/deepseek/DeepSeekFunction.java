package com.roamdeck.backend.infrastructure.itinerary.deepseek;

import com.fasterxml.jackson.databind.JsonNode;

record DeepSeekFunction(
    String name,
    String description,
    boolean strict,
    JsonNode parameters
) {
}