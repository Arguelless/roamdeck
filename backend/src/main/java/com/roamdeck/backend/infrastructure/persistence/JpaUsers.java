package com.roamdeck.backend.infrastructure.persistence;

import com.roamdeck.backend.application.ports.Users;
import com.roamdeck.backend.domain.user.User;
import com.roamdeck.backend.infrastructure.exceptions.EmailAlreadyExistsException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class JpaUsers implements Users {

    private final UserRepository userRepository;

    public JpaUsers(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    @Override
    public User save(User user) {
        try {
            return userRepository.save(user);
        } catch (DataIntegrityViolationException e) {
            if (userRepository.findByEmail(user.getEmail()).isPresent()) {
                throw new EmailAlreadyExistsException(user.getEmail());
            }

            throw e;
        }
    }
}
