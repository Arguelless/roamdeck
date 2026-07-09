package com.roamdeck.backend.infrastructure.itinerary.deepseek;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

record DeepSeekChatRequest(
    String model,
    List<DeepSeekMessage> messages,
    List<DeepSeekTool> tools,
    @JsonProperty("tool_choice") DeepSeekToolChoice toolChoice,
    DeepSeekThinking thinking
) {
}