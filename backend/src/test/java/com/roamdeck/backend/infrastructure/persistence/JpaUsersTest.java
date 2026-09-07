package com.roamdeck.backend.infrastructure.persistence;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;

import com.roamdeck.backend.domain.user.User;
import com.roamdeck.backend.infrastructure.exceptions.EmailAlreadyExistsException;

class JpaUsersTest {

    private final UserRepository userRepository = mock(UserRepository.class);
    private final JpaUsers users = new JpaUsers(userRepository);

    @Test
    void savesAUserThatDoesNotClashWithAnExistingOne() {
        User user = userWith("ana@roamdeck.com");
        given(userRepository.save(any(User.class))).willReturn(user);

        assertThat(users.save(user)).isSameAs(user);
    }

    @Test
    void reportsAConflictWhenTheDatabaseRejectsADuplicatedEmail() {
        User user = userWith("ana@roamdeck.com");
        given(userRepository.save(any(User.class)))
            .willThrow(new DataIntegrityViolationException("duplicate key"));
        given(userRepository.findByEmail("ana@roamdeck.com")).willReturn(Optional.of(user));

        assertThatThrownBy(() -> users.save(user))
            .isInstanceOf(EmailAlreadyExistsException.class)
            .hasMessageContaining("ana@roamdeck.com");
    }

    @Test
    void doesNotBlameTheEmailForAnUnrelatedIntegrityFailure() {
        User user = userWith("ana@roamdeck.com");
        given(userRepository.save(any(User.class)))
            .willThrow(new DataIntegrityViolationException("null value in column"));
        given(userRepository.findByEmail("ana@roamdeck.com")).willReturn(Optional.empty());

        assertThatThrownBy(() -> users.save(user))
            .isInstanceOf(DataIntegrityViolationException.class);
    }

    private User userWith(String email) {
        User user = new User();
        user.setEmail(email);
        user.setPasswordHash("a-hash");
        return user;
    }
}
