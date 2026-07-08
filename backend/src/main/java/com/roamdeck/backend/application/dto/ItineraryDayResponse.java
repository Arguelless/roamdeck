package com.roamdeck.backend.application.dto;

import java.time.LocalDate;
import java.util.List;

public record ItineraryDayResponse(
    LocalDate date,
    List<ActivityResponse> activities
) {
}
