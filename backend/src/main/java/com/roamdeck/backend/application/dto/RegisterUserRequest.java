package com.roamdeck.backend.application.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegisterUserRequest(

        @Email
        @NotBlank
        String email,

        @NotBlank
        @Size(min = RegisterUserRequest.MINIMUM_PASSWORD_LENGTH)
        String password

) {

    public static final int MINIMUM_PASSWORD_LENGTH = 8;
}
