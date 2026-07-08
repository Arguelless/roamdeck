package com.roamdeck.backend.application.ports;

import com.roamdeck.backend.domain.itinerary.Itinerary;
import com.roamdeck.backend.domain.itinerary.TripRequest;

public interface ItineraryGenerator {

    Itinerary generate(TripRequest tripRequest);
}
