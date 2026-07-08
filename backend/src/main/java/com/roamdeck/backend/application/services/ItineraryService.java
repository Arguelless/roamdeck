package com.roamdeck.backend.application.services;

import java.time.LocalDate;
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

        return new TripRequest(
            request.destination(),
            LocalDate.parse(request.startDate()),
            LocalDate.parse(request.endDate()),
            Integer.parseInt(request.budget()),
            preferences
        );
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