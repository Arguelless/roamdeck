package com.roamdeck.backend.application.services;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.jupiter.api.Test;

import com.roamdeck.backend.application.dto.GenerateItineraryRequest;
import com.roamdeck.backend.application.dto.GenerateItineraryResponse;
import com.roamdeck.backend.application.ports.ItineraryGenerator;
import com.roamdeck.backend.domain.itinerary.Activity;
import com.roamdeck.backend.domain.itinerary.Itinerary;
import com.roamdeck.backend.domain.itinerary.ItineraryDay;
import com.roamdeck.backend.domain.itinerary.TripRequest;
import com.roamdeck.backend.infrastructure.exceptions.InvalidTripRequestException;

class ItineraryServiceTest {

    @Test
    void mapsGeneratedItineraryIntoAStructuredResponse() {
        ItineraryGenerator fakeGenerator = tripRequest -> new Itinerary(List.of(
            new ItineraryDay(
                LocalDate.of(2026, 8, 1),
                List.of(new Activity("Mañana", "Visitar el Coliseo"))
            )
        ));
        ItineraryService itineraryService = new ItineraryService(fakeGenerator);

        GenerateItineraryRequest request = new GenerateItineraryRequest(
            "Roma", "2026-08-01", "2026-08-05", "1000", "arte, comida local"
        );

        GenerateItineraryResponse response = itineraryService.generateItinerary(request);

        assertThat(response.days()).hasSize(1);
        assertThat(response.days().get(0).date()).isEqualTo(LocalDate.of(2026, 8, 1));
        assertThat(response.days().get(0).activities()).hasSize(1);
        assertThat(response.days().get(0).activities().get(0).timeOfDay()).isEqualTo("Mañana");
        assertThat(response.days().get(0).activities().get(0).description()).isEqualTo("Visitar el Coliseo");
    }

    @Test
    void mapsRequestDtoIntoDomainTripRequest() {
        AtomicReference<TripRequest> capturedRequest = new AtomicReference<>();
        ItineraryGenerator fakeGenerator = tripRequest -> {
            capturedRequest.set(tripRequest);
            return new Itinerary(List.of());
        };
        ItineraryService itineraryService = new ItineraryService(fakeGenerator);

        GenerateItineraryRequest request = new GenerateItineraryRequest(
            "Roma", "2026-08-01", "2026-08-05", "1000", "arte,  comida local ,"
        );

        itineraryService.generateItinerary(request);

        TripRequest tripRequest = capturedRequest.get();
        assertThat(tripRequest.destination()).isEqualTo("Roma");
        assertThat(tripRequest.startDate()).isEqualTo(LocalDate.of(2026, 8, 1));
        assertThat(tripRequest.endDate()).isEqualTo(LocalDate.of(2026, 8, 5));
        assertThat(tripRequest.budget()).isEqualTo(1000);
        assertThat(tripRequest.preferences()).containsExactly("arte", "comida local");
    }

    @Test
    void rejectsAMalformedStartDate() {
        ItineraryService itineraryService = new ItineraryService(unusedGenerator());

        GenerateItineraryRequest request = new GenerateItineraryRequest(
            "Roma", "not-a-date", "2026-08-05", "1000", ""
        );

        assertThatThrownBy(() -> itineraryService.generateItinerary(request))
            .isInstanceOf(InvalidTripRequestException.class);
    }

    @Test
    void rejectsANonNumericBudget() {
        ItineraryService itineraryService = new ItineraryService(unusedGenerator());

        GenerateItineraryRequest request = new GenerateItineraryRequest(
            "Roma", "2026-08-01", "2026-08-05", "not-a-number", ""
        );

        assertThatThrownBy(() -> itineraryService.generateItinerary(request))
            .isInstanceOf(InvalidTripRequestException.class);
    }

    @Test
    void rejectsAnEndDateBeforeTheStartDate() {
        ItineraryService itineraryService = new ItineraryService(unusedGenerator());

        GenerateItineraryRequest request = new GenerateItineraryRequest(
            "Roma", "2026-08-05", "2026-08-01", "1000", ""
        );

        assertThatThrownBy(() -> itineraryService.generateItinerary(request))
            .isInstanceOf(InvalidTripRequestException.class);
    }

    private ItineraryGenerator unusedGenerator() {
        return tripRequest -> {
            throw new AssertionError("The generator should not be called for an invalid request");
        };
    }
}
