package com.roamdeck.backend.infrastructure.itinerary.deepseek;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "deepseek")
public record DeepSeekProperties(String baseUrl, String apiKey, String model) {
}