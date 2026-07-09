package com.roamdeck.backend.infrastructure.itinerary.deepseek;

record DeepSeekToolChoice(
    String type,
    DeepSeekToolChoiceFunction function
) {
}