package com.roamdeck.backend.infrastructure.itinerary.deepseek;

import java.time.LocalDate;
import java.util.List;

import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.roamdeck.backend.application.ports.ItineraryGenerator;
import com.roamdeck.backend.domain.itinerary.Activity;
import com.roamdeck.backend.domain.itinerary.Itinerary;
import com.roamdeck.backend.domain.itinerary.ItineraryDay;
import com.roamdeck.backend.domain.itinerary.TripRequest;
import com.roamdeck.backend.infrastructure.exceptions.ItineraryGenerationException;

@Component
@Profile("deepseek")
public class DeepSeekItineraryGenerator implements ItineraryGenerator {
    private static final String FUNCTION_NAME = "generate_itinerary";

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
                          "required": ["timeOfDay", "description"],
                          "additionalProperties": false
                        }
                      }
                    },
                    "required": ["date", "activities"],
                    "additionalProperties": false
                  }
                }
              },
              "required": ["days"],
              "additionalProperties": false
            }
            """;

    private final RestClient restClient;
    private final ObjectMapper objectMapper;
    private final DeepSeekProperties properties;
    private final JsonNode schema;

    public DeepSeekItineraryGenerator(DeepSeekProperties properties, ObjectMapper objectMapper) {
        this.properties = properties;
        this.objectMapper = objectMapper;
        this.restClient = RestClient.builder()
                .baseUrl(properties.baseUrl())
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + properties.apiKey())
                .build();
        try {
            this.schema = objectMapper.readTree(ITINERARY_JSON_SCHEMA);
        } catch (Exception e) {
            throw new IllegalStateException("Schema de itinerario mal formado", e);
        }
    }

    @Override
    public Itinerary generate(TripRequest tripRequest) {
        try {
            DeepSeekFunction function = new DeepSeekFunction(
                    FUNCTION_NAME,
                    "Genera un itinerario de viaje estructurado",
                    true,
                    schema);

            DeepSeekChatRequest request = new DeepSeekChatRequest(
                    properties.model(),
                    List.of(new DeepSeekMessage("user", buildPrompt(tripRequest))),
                    List.of(new DeepSeekTool("function", function)),
                    new DeepSeekToolChoice("function", new DeepSeekToolChoiceFunction(FUNCTION_NAME)),
                    new DeepSeekThinking("disabled"));

            DeepSeekChatResponse response = restClient.post()
                    .uri("/chat/completions")
                    .body(request)
                    .retrieve()
                    .body(DeepSeekChatResponse.class);

            return parseItinerary(response);
        } catch (Exception e) {
            throw new ItineraryGenerationException("No se pudo generar el itinerario con DeepSeek: " + e.getMessage());
        }
    }

    private String buildPrompt(TripRequest tripRequest) {
        return """
                Genera un itinerario de viaje a %s, desde %s hasta %s, con presupuesto %d.
                Preferencias: %s.
                """.formatted(
                tripRequest.destination(),
                tripRequest.startDate(),
                tripRequest.endDate(),
                tripRequest.budget(),
                String.join(", ", tripRequest.preferences()));
    }

    private Itinerary parseItinerary(DeepSeekChatResponse response) throws JsonProcessingException {
        String argumentsJson = response.choices().get(0).message().toolCalls().get(0).function().arguments();
        DeepSeekItineraryPayload payload = objectMapper.readValue(argumentsJson, DeepSeekItineraryPayload.class);

        List<ItineraryDay> days = payload.days().stream()
                .map(this::toDomain)
                .toList();

        return new Itinerary(days);
    }

    private ItineraryDay toDomain(DeepSeekDayPayload day) {
        List<Activity> activities = day.activities().stream()
                .map(a -> new Activity(a.timeOfDay(), a.description()))
                .toList();

        return new ItineraryDay(LocalDate.parse(day.date()), activities);
    }
}