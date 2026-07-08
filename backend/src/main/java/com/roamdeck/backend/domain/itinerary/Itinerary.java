package com.roamdeck.backend.domain.itinerary;

import java.util.List;

public record Itinerary(
    List<ItineraryDay> days
) {
    
}
