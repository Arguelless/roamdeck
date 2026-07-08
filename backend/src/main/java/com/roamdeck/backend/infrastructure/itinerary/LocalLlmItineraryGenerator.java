package com.roamdeck.backend.infrastructure.itinerary;

import java.time.LocalDate;
import java.util.List;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.roamdeck.backend.application.ports.ItineraryGenerator;
import com.roamdeck.backend.domain.itinerary.Activity;
import com.roamdeck.backend.domain.itinerary.Itinerary;
import com.roamdeck.backend.domain.itinerary.ItineraryDay;
import com.roamdeck.backend.domain.itinerary.TripRequest;
import com.roamdeck.backend.infrastructure.exceptions.ItineraryGenerationException;

@Component
@Profile("local")
public class LocalLlmItineraryGenerator implements ItineraryGenerator {

    private static final String ITINERARY_JSON_SCHEMA = """
        {
          "type": "object",
          "properties": {
            "days": {
              "type": "array",
              "items": {
                "type": "object",
                "properties": {
                  "date": { "type": "string" },
                  "activities": {
                    "type": "array",
                    "items": {
                      "type": "object",
                      "properties": {
                        "timeOfDay": { "type": "string", "enum": ["Mañana", "Tarde", "Noche"] },
                        "description": { "type": "string" }
                      },
                      "required": ["timeOfDay", "description"]
                    }
                  }
                },
                "required": ["date", "activities"]
              }
            }
          },
          "required": ["days"]
        }
        """;

    private final RestClient restClient;
    private final ObjectMapper objectMapper;
    private final OllamaProperties properties;
    private final JsonNode schema;

    public LocalLlmItineraryGenerator(OllamaProperties properties, ObjectMapper objectMapper) {
        this.properties = properties;
        this.objectMapper = objectMapper;
        this.restClient = RestClient.builder()
            .baseUrl(properties.baseUrl())
            .build();
        try {
            this.schema = objectMapper.readTree(ITINERARY_JSON_SCHEMA);
        } catch (Exception e) {
            throw new IllegalStateException("Schema de itinerario mal formado", e);
        }
    }

    @Override
    public Itinerary generate(TripRequest tripRequest) {
        OllamaGenerateRequest request = new OllamaGenerateRequest(
            properties.model(),
            buildPrompt(tripRequest),
            schema,
            new OllamaOptions(0.2),
            false,
            false
        );

        OllamaGenerateResponse response = restClient.post()
            .uri("/api/generate")
            .body(request)
            .retrieve()
            .body(OllamaGenerateResponse.class);

        return parseItinerary(response.response());
    }

    private String buildPrompt(TripRequest tripRequest) {
        return """
            Genera un itinerario de viaje a %s, desde %s hasta %s, con presupuesto %d.
            Preferencias: %s.
            Responde solo con el itinerario, sin comentarios adicionales.
            """.formatted(
                tripRequest.destination(),
                tripRequest.startDate(),
                tripRequest.endDate(),
                tripRequest.budget(),
                String.join(", ", tripRequest.preferences())
            );
    }

    private Itinerary parseItinerary(String rawJson) {
        try {
            OllamaItineraryPayload payload = objectMapper.readValue(rawJson, OllamaItineraryPayload.class);

            List<ItineraryDay> days = payload.days().stream()
                .map(this::toDomain)
                .toList();

            return new Itinerary(days);
        } catch (Exception e) {
            throw new ItineraryGenerationException("No se pudo interpretar la respuesta del modelo local: " + e.getMessage());
        }
    }

    private ItineraryDay toDomain(OllamaDayPayload day) {
        List<Activity> activities = day.activities().stream()
            .map(a -> new Activity(a.timeOfDay(), a.description()))
            .toList();

        return new ItineraryDay(LocalDate.parse(day.date()), activities);
    }
}