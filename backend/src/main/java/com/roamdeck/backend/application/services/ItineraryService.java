package com.roamdeck.backend.application.services;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.Arrays;
import java.util.List;

import org.springframework.stereotype.Service;

import com.roamdeck.backend.application.dto.ActivityResponse;
import com.roamdeck.backend.application.dto.GenerateItineraryRequest;
import com.roamdeck.backend.application.dto.GenerateItineraryResponse;
import com.roamdeck.backend.application.dto.ItineraryDayResponse;
import com.roamdeck.backend.application.ports.ItineraryGenerator;
import com.roamdeck.backend.domain.itinerary.Activity;
import com.roamdeck.backend.domain.itinerary.Itinerary;
import com.roamdeck.backend.domain.itinerary.ItineraryDay;
import com.roamdeck.backend.domain.itinerary.TripRequest;
import com.roamdeck.backend.infrastructure.exceptions.InvalidTripRequestException;

@Service
public class ItineraryService {

    private final ItineraryGenerator itineraryGenerator;

    public ItineraryService(ItineraryGenerator itineraryGenerator) {
        this.itineraryGenerator = itineraryGenerator;
    }

    public GenerateItineraryResponse generateItinerary(GenerateItineraryRequest request) {
        TripRequest tripRequest = toDomain(request);
        Itinerary itinerary = itineraryGenerator.generate(tripRequest);
        return toResponse(itinerary);
    }

    private TripRequest toDomain(GenerateItineraryRequest request) {
        List<String> preferences = Arrays.stream(request.preferences().split(","))
            .map(String::trim)
            .filter(preference -> !preference.isEmpty())
            .toList();

        LocalDate startDate = parseDate(request.startDate(), "startDate");
        LocalDate endDate = parseDate(request.endDate(), "endDate");

        if (endDate.isBefore(startDate)) {
            throw new InvalidTripRequestException("endDate must not be before startDate");
        }

        return new TripRequest(
            request.destination(),
            startDate,
            endDate,
            parseBudget(request.budget()),
            preferences
        );
    }

    private LocalDate parseDate(String value, String fieldName) {
        try {
            return LocalDate.parse(value);
        } catch (DateTimeParseException e) {
            throw new InvalidTripRequestException("Invalid " + fieldName + ": '" + value + "' is not a valid date (expected yyyy-MM-dd)");
        }
    }

    private int parseBudget(String value) {
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            throw new InvalidTripRequestException("Invalid budget: '" + value + "' is not a valid number");
        }
    }

    private GenerateItineraryResponse toResponse(Itinerary itinerary) {
        List<ItineraryDayResponse> days = itinerary.days().stream()
            .map(this::toResponse)
            .toList();

        return new GenerateItineraryResponse(days);
    }

    private ItineraryDayResponse toResponse(ItineraryDay day) {
        List<ActivityResponse> activities = day.activities().stream()
            .map(this::toResponse)
            .toList();

        return new ItineraryDayResponse(day.date(), activities);
    }

    private ActivityResponse toResponse(Activity activity) {
        return new ActivityResponse(activity.timeOfDay(), activity.description());
    }
}