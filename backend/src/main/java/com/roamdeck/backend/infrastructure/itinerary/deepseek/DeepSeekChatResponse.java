package com.roamdeck.backend.infrastructure.itinerary.deepseek;

import java.util.List;

record DeepSeekChatResponse(
    List<DeepSeekChoice> choices
) {
}
