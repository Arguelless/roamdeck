package com.roamdeck.backend.application.services;

import com.roamdeck.backend.application.dto.AuthResponse;
import com.roamdeck.backend.application.dto.LoginRequest;
import com.roamdeck.backend.application.ports.TokenGenerator;
import com.roamdeck.backend.domain.user.User;
import com.roamdeck.backend.infrastructure.exceptions.InvalidCredentialsException;
import com.roamdeck.backend.infrastructure.persistence.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final TokenGenerator tokenGenerator;

    public AuthService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       TokenGenerator tokenGenerator) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.tokenGenerator = tokenGenerator;
    }

    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.email())
                .orElseThrow(InvalidCredentialsException::new);

        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new InvalidCredentialsException();
        }

        return new AuthResponse(tokenGenerator.generate(user));
    }
}