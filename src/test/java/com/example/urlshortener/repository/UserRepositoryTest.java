package com.example.urlshortener.repository;

import com.example.urlshortener.entity.User;
import java.time.Instant;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Test
    void savesAndFindsUserByEmail() {
        User user = User.builder()
                .email("jpa-test-user@example.com")
                .password("P@ssw0rd")
                .createdAt(Instant.now())
                .build();

        userRepository.save(user);

        Assertions.assertThat(userRepository.findByEmail("jpa-test-user@example.com")).isPresent();
    }

    @Test
    void reportsExistingAndMissingEmail() {
        User user = User.builder()
                .email("jpa-test-exists@example.com")
                .password("P@ssw0rd")
                .createdAt(Instant.now())
                .build();

        userRepository.save(user);

        Assertions.assertThat(userRepository.existsByEmail("jpa-test-exists@example.com")).isTrue();
        Assertions.assertThat(userRepository.existsByEmail("jpa-test-missing@example.com")).isFalse();
    }
}
