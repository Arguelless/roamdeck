package com.roamdeck.backend.infrastructure.itinerary.deepseek;

record DeepSeekToolCall(
    String id,
    String type,
    DeepSeekToolCallFunction function
) {
}
