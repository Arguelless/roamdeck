package com.roamdeck.backend.application.ports;

import com.roamdeck.backend.domain.user.User;

public interface TokenGenerator {
    String generate(User user);
}