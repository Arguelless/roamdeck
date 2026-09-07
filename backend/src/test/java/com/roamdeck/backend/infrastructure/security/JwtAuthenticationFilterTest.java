package com.roamdeck.backend.infrastructure.security;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import javax.crypto.SecretKey;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.roamdeck.backend.application.dto.GenerateItineraryResponse;
import com.roamdeck.backend.application.services.ItineraryService;
import com.roamdeck.backend.domain.user.User;
import com.roamdeck.backend.infrastructure.controllers.ItineraryController;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

@WebMvcTest(ItineraryController.class)
@Import({SecurityConfig.class, JwtAuthenticationFilter.class, JwtTokenGenerator.class})
@EnableConfigurationProperties(JwtProperties.class)
@TestPropertySource(properties = {
    "jwt.secret=" + JwtAuthenticationFilterTest.SECRET,
    "jwt.expiration-minutes=60"
})
class JwtAuthenticationFilterTest {

    static final String SECRET = "a-test-secret-that-is-at-least-32-bytes-long";
    private static final String ANOTHER_SECRET = "a-different-secret-also-at-least-32-bytes-long";

    private static final String PROTECTED_ENDPOINT = "/api/itineraries/generate";
    private static final String REQUEST_BODY = """
        {"destination":"Lisboa","startDate":"2026-04-01","endDate":"2026-04-04","budget":"medium","preferences":""}
        """;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtTokenGenerator tokenGenerator;

    @MockitoBean
    private ItineraryService itineraryService;

    @Test
    void letsThroughARequestCarryingAValidToken() throws Exception {
        given(itineraryService.generateItinerary(any()))
            .willReturn(new GenerateItineraryResponse(List.of()));

        mockMvc.perform(generateItineraryWith(tokenGenerator.generate(anExistingUser())))
            .andExpect(status().isCreated());
    }

    @Test
    void rejectsARequestWithoutAToken() throws Exception {
        mockMvc.perform(post(PROTECTED_ENDPOINT)
                .contentType(MediaType.APPLICATION_JSON)
                .content(REQUEST_BODY))
            .andExpect(status().isUnauthorized());
    }

    @Test
    void rejectsAMalformedToken() throws Exception {
        mockMvc.perform(generateItineraryWith("not-even-a-jwt"))
            .andExpect(status().isUnauthorized());
    }

    @Test
    void rejectsATokenSignedWithAnotherSecret() throws Exception {
        mockMvc.perform(generateItineraryWith(tokenSignedWith(ANOTHER_SECRET, Instant.now().plus(1, ChronoUnit.HOURS))))
            .andExpect(status().isUnauthorized());
    }

    @Test
    void rejectsAnExpiredToken() throws Exception {
        mockMvc.perform(generateItineraryWith(tokenSignedWith(SECRET, Instant.now().minus(1, ChronoUnit.HOURS))))
            .andExpect(status().isUnauthorized());
    }

    private org.springframework.test.web.servlet.RequestBuilder generateItineraryWith(String token) {
        return post(PROTECTED_ENDPOINT)
            .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
            .contentType(MediaType.APPLICATION_JSON)
            .content(REQUEST_BODY);
    }

    private User anExistingUser() {
        User user = new User();
        user.setId(UUID.randomUUID());
        user.setEmail("ana@roamdeck.com");
        return user;
    }

    private String tokenSignedWith(String secret, Instant expiration) {
        SecretKey key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        return Jwts.builder()
            .subject(UUID.randomUUID().toString())
            .expiration(Date.from(expiration))
            .signWith(key)
            .compact();
    }
}
