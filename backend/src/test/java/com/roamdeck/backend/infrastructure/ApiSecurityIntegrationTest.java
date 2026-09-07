package com.roamdeck.backend.infrastructure;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ApiSecurityIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    void answersBadRequestWhenTheRegistrationIsInvalid() {
        ResponseEntity<String> response = register("{\"email\":\"eva@roamdeck.com\",\"password\":\"short\"}");

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void answersBadRequestWhenTheEmailIsNotAnEmail() {
        ResponseEntity<String> response = register("{\"email\":\"not-an-email\",\"password\":\"secret123\"}");

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void keepsProtectingTheEndpointsThatNeedAToken() {
        ResponseEntity<String> response = restTemplate.postForEntity(
            "/api/itineraries/generate", jsonPayload("{}"), String.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    private ResponseEntity<String> register(String body) {
        return restTemplate.postForEntity("/api/users/register", jsonPayload(body), String.class);
    }

    private HttpEntity<String> jsonPayload(String body) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        return new HttpEntity<>(body, headers);
    }
}
