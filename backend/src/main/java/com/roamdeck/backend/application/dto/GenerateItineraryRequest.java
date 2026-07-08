package com.roamdeck.backend.application.dto;


public record GenerateItineraryRequest(
    String destination,
    String startDate,
    String endDate,
    String budget,
    String preferences
) {
}