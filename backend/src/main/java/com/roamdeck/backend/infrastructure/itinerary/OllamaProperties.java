package com.roamdeck.backend.infrastructure.itinerary;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "ollama")
public record OllamaProperties(String baseUrl, String model) {
}