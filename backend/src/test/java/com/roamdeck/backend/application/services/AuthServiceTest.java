package com.roamdeck.backend.application.services;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.roamdeck.backend.application.dto.AuthResponse;
import com.roamdeck.backend.application.dto.LoginRequest;
import com.roamdeck.backend.application.ports.TokenGenerator;
import com.roamdeck.backend.application.ports.Users;
import com.roamdeck.backend.domain.user.User;
import com.roamdeck.backend.infrastructure.exceptions.InvalidCredentialsException;

class AuthServiceTest {

    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Test
    void returnsATokenWhenTheCredentialsAreValid() {
        User user = userWith("ana@roamdeck.com", "secret");
        AuthService authService = new AuthService(
            usersContaining(user), passwordEncoder, u -> "a-signed-token"
        );

        AuthResponse response = authService.login(
            new LoginRequest("ana@roamdeck.com", "secret")
        );

        assertThat(response.token()).isEqualTo("a-signed-token");
    }

    @Test
    void issuesTheTokenForTheAuthenticatedUser() {
        User user = userWith("ana@roamdeck.com", "secret");
        AtomicReference<User> capturedUser = new AtomicReference<>();
        TokenGenerator capturingGenerator = u -> {
            capturedUser.set(u);
            return "a-signed-token";
        };
        AuthService authService = new AuthService(
            usersContaining(user), passwordEncoder, capturingGenerator
        );

        authService.login(new LoginRequest("ana@roamdeck.com", "secret"));

        assertThat(capturedUser.get().getEmail()).isEqualTo("ana@roamdeck.com");
    }

    @Test
    void rejectsAnUnknownEmail() {
        AuthService authService = new AuthService(
            noUsers(), passwordEncoder, unusedGenerator()
        );

        assertThatThrownBy(() -> authService.login(
            new LoginRequest("nobody@roamdeck.com", "secret")
        )).isInstanceOf(InvalidCredentialsException.class);
    }

    @Test
    void rejectsAWrongPassword() {
        User user = userWith("ana@roamdeck.com", "secret");
        AuthService authService = new AuthService(
            usersContaining(user), passwordEncoder, unusedGenerator()
        );

        assertThatThrownBy(() -> authService.login(
            new LoginRequest("ana@roamdeck.com", "not-the-password")
        )).isInstanceOf(InvalidCredentialsException.class);
    }

    private User userWith(String email, String rawPassword) {
        User user = new User();
        user.setEmail(email);
        user.setPasswordHash(passwordEncoder.encode(rawPassword));
        return user;
    }

    private Users usersContaining(User user) {
        return new FakeUsers(user);
    }

    private Users noUsers() {
        return new FakeUsers(null);
    }

    private TokenGenerator unusedGenerator() {
        return user -> {
            throw new AssertionError("No token should be issued for invalid credentials");
        };
    }

    private record FakeUsers(User stored) implements Users {

        @Override
        public Optional<User> findByEmail(String email) {
            return Optional.ofNullable(stored)
                .filter(user -> user.getEmail().equals(email));
        }

        @Override
        public User save(User user) {
            throw new AssertionError("Login should not save users");
        }
    }
}
