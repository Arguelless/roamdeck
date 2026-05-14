package com.roamdeck.backend.application.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record RegisterUserRequest(

        @Email
        @NotBlank
        String email,

        @NotBlank
        String password

) {
}