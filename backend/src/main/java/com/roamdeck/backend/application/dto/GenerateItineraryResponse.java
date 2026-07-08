package com.roamdeck.backend.application.dto;

import java.util.List;

public record GenerateItineraryResponse(
    List<ItineraryDayResponse> days
) {}