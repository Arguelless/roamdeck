package com.roamdeck.backend.infrastructure.controllers;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.roamdeck.backend.application.dto.GenerateItineraryRequest;
import com.roamdeck.backend.application.dto.GenerateItineraryResponse;
import com.roamdeck.backend.application.services.ItineraryService;

@RestController
@RequestMapping("/api/itineraries")
public class ItineraryController {
    private final ItineraryService itineraryService;
    public ItineraryController(ItineraryService itineraryService) {
        this.itineraryService = itineraryService;
    }

    @PostMapping("/generate")
    public ResponseEntity<GenerateItineraryResponse> generateItinerary(@RequestBody GenerateItineraryRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(itineraryService.generateItinerary(request));
    }
}