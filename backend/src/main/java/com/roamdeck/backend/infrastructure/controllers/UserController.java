package com.roamdeck.backend.infrastructure.controllers;

import com.roamdeck.backend.application.dto.RegisterUserRequest;
import com.roamdeck.backend.application.services.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/register")
    public ResponseEntity<String> register(
            @Valid @RequestBody RegisterUserRequest request
    ) {

        userService.register(request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body("User registered successfully");
    }
}