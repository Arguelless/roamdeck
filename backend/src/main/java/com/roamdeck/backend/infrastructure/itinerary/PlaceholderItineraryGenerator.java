package com.roamdeck.backend.infrastructure.itinerary;

import java.util.List;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import com.roamdeck.backend.application.ports.ItineraryGenerator;
import com.roamdeck.backend.domain.itinerary.Activity;
import com.roamdeck.backend.domain.itinerary.Itinerary;
import com.roamdeck.backend.domain.itinerary.ItineraryDay;
import com.roamdeck.backend.domain.itinerary.TripRequest;

/**
 * Temporary stand-in for the real AI-backed generator. Produces a single
 * placeholder day so the end-to-end contract can be exercised before the
 * Anthropic-backed adapter is wired in.
 */
@Component
@Profile("dev")
public class PlaceholderItineraryGenerator implements ItineraryGenerator {

    @Override
    public Itinerary generate(TripRequest tripRequest) {
        Activity activity = new Activity(
            "MORNING",
            "Explorar " + tripRequest.destination()
        );

        ItineraryDay day = new ItineraryDay(tripRequest.startDate(), List.of(activity));

        return new Itinerary(List.of(day));
    }
}
