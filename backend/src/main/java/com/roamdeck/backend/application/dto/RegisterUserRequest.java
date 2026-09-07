package com.roamdeck.backend.application.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegisterUserRequest(

        @Email(message = "must be a well-formed email address")
        @NotBlank(message = "must not be blank")
        String email,

        @NotBlank(message = "must not be blank")
        @Size(min = RegisterUserRequest.MINIMUM_PASSWORD_LENGTH,
              message = "must be at least " + RegisterUserRequest.MINIMUM_PASSWORD_LENGTH + " characters")
        String password

) {

    public static final int MINIMUM_PASSWORD_LENGTH = 8;
}
