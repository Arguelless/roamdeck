package com.roamdeck.backend.domain.itinerary;

import java.time.LocalDate;
import java.util.List;

public record ItineraryDay(
    LocalDate date,
    List<Activity> activities
) {
    
}
