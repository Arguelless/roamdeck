package com.roamdeck.backend.application.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record LoginRequest(

        @Email(message = "must be a well-formed email address")
        @NotBlank(message = "must not be blank")
        String email,

        @NotBlank(message = "must not be blank")
        String password

) {
}
