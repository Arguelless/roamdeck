package com.roamdeck.backend.application.services;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.roamdeck.backend.application.dto.RegisterUserRequest;
import com.roamdeck.backend.application.ports.Users;
import com.roamdeck.backend.domain.user.User;
import com.roamdeck.backend.infrastructure.exceptions.EmailAlreadyExistsException;

class UserServiceTest {

    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Test
    void storesTheNewUserWithTheHashedPassword() {
        FakeUsers users = new FakeUsers();
        UserService userService = new UserService(users, passwordEncoder);

        userService.register(new RegisterUserRequest("ana@roamdeck.com", "secret123"));

        User stored = users.saved().get(0);
        assertThat(stored.getEmail()).isEqualTo("ana@roamdeck.com");
        assertThat(stored.getPasswordHash()).isNotEqualTo("secret123");
        assertThat(passwordEncoder.matches("secret123", stored.getPasswordHash())).isTrue();
    }

    @Test
    void rejectsAnEmailThatIsAlreadyRegistered() {
        FakeUsers users = new FakeUsers();
        users.add(userWith("ana@roamdeck.com"));
        UserService userService = new UserService(users, passwordEncoder);

        assertThatThrownBy(() -> userService.register(
            new RegisterUserRequest("ana@roamdeck.com", "secret123")
        )).isInstanceOf(EmailAlreadyExistsException.class);

        assertThat(users.saved()).isEmpty();
    }

    @Test
    void reportsTheConflictRaisedByTheStoreWhenTheEmailIsTakenMeanwhile() {
        Users usersRejectingTheSave = new Users() {

            @Override
            public Optional<User> findByEmail(String email) {
                return Optional.empty();
            }

            @Override
            public User save(User user) {
                throw new EmailAlreadyExistsException(user.getEmail());
            }
        };
        UserService userService = new UserService(usersRejectingTheSave, passwordEncoder);

        assertThatThrownBy(() -> userService.register(
            new RegisterUserRequest("ana@roamdeck.com", "secret123")
        )).isInstanceOf(EmailAlreadyExistsException.class);
    }

    private User userWith(String email) {
        User user = new User();
        user.setEmail(email);
        user.setPasswordHash(passwordEncoder.encode("whatever"));
        return user;
    }

    private static final class FakeUsers implements Users {

        private final List<User> existing = new ArrayList<>();
        private final List<User> saved = new ArrayList<>();

        void add(User user) {
            existing.add(user);
        }

        List<User> saved() {
            return saved;
        }

        @Override
        public Optional<User> findByEmail(String email) {
            return existing.stream().filter(user -> user.getEmail().equals(email)).findFirst();
        }

        @Override
        public User save(User user) {
            saved.add(user);
            existing.add(user);
            return user;
        }
    }
}
