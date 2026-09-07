package com.roamdeck.backend.application.services;

import com.roamdeck.backend.application.dto.RegisterUserRequest;
import com.roamdeck.backend.application.ports.Users;
import com.roamdeck.backend.domain.user.User;
import com.roamdeck.backend.infrastructure.exceptions.EmailAlreadyExistsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    private final Users users;
    private final PasswordEncoder passwordEncoder;

    public UserService(Users users, PasswordEncoder passwordEncoder) {
        this.users = users;
        this.passwordEncoder = passwordEncoder;
    }

    public void register(RegisterUserRequest request) {

        if (users.findByEmail(request.email()).isPresent()) {
            throw new EmailAlreadyExistsException(request.email());
        }

        User user = new User();

        user.setEmail(request.email());

        user.setPasswordHash(
                passwordEncoder.encode(request.password())
        );

        users.save(user);
    }
}
