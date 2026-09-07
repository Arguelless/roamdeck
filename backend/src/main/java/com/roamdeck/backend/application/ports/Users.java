package com.roamdeck.backend.application.ports;

import com.roamdeck.backend.domain.user.User;

import java.util.Optional;

public interface Users {

    Optional<User> findByEmail(String email);

    User save(User user);
}