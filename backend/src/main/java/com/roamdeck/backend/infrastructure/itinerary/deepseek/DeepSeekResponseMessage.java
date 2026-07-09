package com.roamdeck.backend.infrastructure.itinerary.deepseek;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

record DeepSeekResponseMessage(
    String role,
    String content,
    @JsonProperty("tool_calls") List<DeepSeekToolCall> toolCalls
) {
}
