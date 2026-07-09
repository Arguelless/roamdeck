package com.roamdeck.backend.infrastructure.itinerary.deepseek;

import java.util.List;

record DeepSeekItineraryPayload(
    List<DeepSeekDayPayload> days
) {
}
