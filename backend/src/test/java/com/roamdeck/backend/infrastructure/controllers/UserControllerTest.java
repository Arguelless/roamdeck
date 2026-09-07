package com.roamdeck.backend.infrastructure.controllers;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.roamdeck.backend.application.services.UserService;
import com.roamdeck.backend.infrastructure.security.JwtAuthenticationFilter;
import com.roamdeck.backend.infrastructure.security.JwtProperties;
import com.roamdeck.backend.infrastructure.security.JwtTokenGenerator;
import com.roamdeck.backend.infrastructure.security.SecurityConfig;

@WebMvcTest(UserController.class)
@Import({SecurityConfig.class, JwtAuthenticationFilter.class, JwtTokenGenerator.class})
@EnableConfigurationProperties(JwtProperties.class)
@TestPropertySource(properties = {
    "jwt.secret=a-test-secret-that-is-at-least-32-bytes-long",
    "jwt.expiration-minutes=60"
})
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserService userService;

    @Test
    void registersANewUserWithoutAskingForAToken() throws Exception {
        mockMvc.perform(registrationOf("ana@roamdeck.com", "secret123"))
            .andExpect(status().isCreated())
            .andExpect(content().string(""));
    }

    @Test
    void rejectsAPasswordThatIsTooShort() throws Exception {
        mockMvc.perform(registrationOf("ana@roamdeck.com", "short"))
            .andExpect(status().isBadRequest());
    }

    @Test
    void rejectsAnEmailWithoutAValidFormat() throws Exception {
        mockMvc.perform(registrationOf("not-an-email", "secret123"))
            .andExpect(status().isBadRequest());
    }

    private org.springframework.test.web.servlet.RequestBuilder registrationOf(String email, String password) {
        return post("/api/users/register")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"email\":\"" + email + "\",\"password\":\"" + password + "\"}");
    }
}
