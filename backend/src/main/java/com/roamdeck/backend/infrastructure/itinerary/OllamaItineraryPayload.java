package com.roamdeck.backend.infrastructure.itinerary;

import java.util.List;

public record OllamaItineraryPayload(
    List<OllamaDayPayload> days
) {
}