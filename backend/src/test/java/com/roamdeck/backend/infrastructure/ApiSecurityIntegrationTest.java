package com.roamdeck.backend.infrastructure;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import com.roamdeck.backend.domain.user.User;
import com.roamdeck.backend.infrastructure.security.JwtTokenGenerator;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ApiSecurityIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private JwtTokenGenerator tokenGenerator;

    @Test
    void answersBadRequestWhenThePasswordIsTooShort() {
        ResponseEntity<String> response = register("{\"email\":\"eva@roamdeck.com\",\"password\":\"short\"}");

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).contains("password: must be at least 8 characters");
    }

    @Test
    void answersBadRequestWhenTheEmailIsNotAnEmail() {
        ResponseEntity<String> response = register("{\"email\":\"not-an-email\",\"password\":\"secret123\"}");

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).contains("email: must be a well-formed email address");
    }

    @Test
    void keepsProtectingTheEndpointsThatNeedAToken() {
        ResponseEntity<String> response = restTemplate.postForEntity(
            "/api/itineraries/generate", jsonPayload("{}"), String.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void answersNotFoundInsteadOfUnauthorizedWhenAnAuthenticatedCallerMissesThePath() {
        HttpHeaders headers = jsonHeaders();
        headers.setBearerAuth(tokenGenerator.generate(anExistingUser()));

        ResponseEntity<String> response = restTemplate.postForEntity(
            "/api/there-is-nothing-here", new HttpEntity<>("{}", headers), String.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    private ResponseEntity<String> register(String body) {
        return restTemplate.postForEntity("/api/users/register", jsonPayload(body), String.class);
    }

    private User anExistingUser() {
        User user = new User();
        user.setId(UUID.randomUUID());
        user.setEmail("ana@roamdeck.com");
        return user;
    }

    private HttpEntity<String> jsonPayload(String body) {
        return new HttpEntity<>(body, jsonHeaders());
    }

    private HttpHeaders jsonHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        return headers;
    }
}
