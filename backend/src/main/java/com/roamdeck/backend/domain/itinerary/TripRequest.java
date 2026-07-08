package com.roamdeck.backend.domain.itinerary;

import java.time.LocalDate;
import java.util.List;

public record TripRequest(
    String destination,
    LocalDate startDate,
    LocalDate endDate,
    Integer budget,
    List<String> preferences
) {
}
