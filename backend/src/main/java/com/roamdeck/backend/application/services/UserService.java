package com.roamdeck.backend.application.services;

import com.roamdeck.backend.application.dto.RegisterUserRequest;
import com.roamdeck.backend.domain.user.User;
import com.roamdeck.backend.infrastructure.exceptions.EmailAlreadyExistsException;
import com.roamdeck.backend.infrastructure.persistence.UserRepository;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
        this.passwordEncoder = new BCryptPasswordEncoder();
    }

    public void register(RegisterUserRequest request) {

        if (userRepository.findByEmail(request.email()).isPresent()) {
            throw new EmailAlreadyExistsException(request.email());
        }

        User user = new User();

        user.setEmail(request.email());

        user.setPasswordHash(
                passwordEncoder.encode(request.password())
        );

        userRepository.save(user);
    }
}