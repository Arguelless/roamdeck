package com.roamdeck.backend.infrastructure.itinerary.ollama;

import java.util.List;

record OllamaItineraryPayload(
    List<OllamaDayPayload> days
) {
}