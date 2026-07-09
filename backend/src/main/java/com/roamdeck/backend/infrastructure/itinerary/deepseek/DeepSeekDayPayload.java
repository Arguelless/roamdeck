package com.roamdeck.backend.infrastructure.itinerary.deepseek;

import java.util.List;

record DeepSeekDayPayload(
    String date,
    List<DeepSeekActivityPayload> activities
) {
}
