package com.example.urlshortener.repository;

import com.example.urlshortener.entity.ShortUrl;
import com.example.urlshortener.entity.User;
import java.time.Instant;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class ShortUrlRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ShortUrlRepository shortUrlRepository;

    private User persistOwner(String email) {
        return userRepository.save(User.builder()
                .email(email)
                .password("P@ssw0rd")
                .createdAt(Instant.now())
                .build());
    }

    @Test
    void findsActiveShortUrlByCode() {
        User owner = persistOwner("jpa-test-owner-1@example.com");
        shortUrlRepository.save(ShortUrl.builder()
                .shortCode("jpa001")
                .originalUrl("https://example.com/jpa-active")
                .owner(owner)
                .active(true)
                .build());

        Assertions.assertThat(shortUrlRepository.findByShortCodeAndActiveTrue("jpa001")).isPresent();
    }

    @Test
    void doesNotFindInactiveShortUrlByCode() {
        User owner = persistOwner("jpa-test-owner-2@example.com");
        shortUrlRepository.save(ShortUrl.builder()
                .shortCode("jpa002")
                .originalUrl("https://example.com/jpa-inactive")
                .owner(owner)
                .active(false)
                .build());

        Assertions.assertThat(shortUrlRepository.findByShortCodeAndActiveTrue("jpa002")).isEmpty();
    }

    @Test
    void listsOnlyShortUrlsOwnedByGivenOwner() {
        User ownerA = persistOwner("jpa-test-owner-a@example.com");
        User ownerB = persistOwner("jpa-test-owner-b@example.com");

        shortUrlRepository.save(ShortUrl.builder()
                .shortCode("jpa00a")
                .originalUrl("https://example.com/owner-a")
                .owner(ownerA)
                .build());
        shortUrlRepository.save(ShortUrl.builder()
                .shortCode("jpa00b")
                .originalUrl("https://example.com/owner-b")
                .owner(ownerB)
                .build());

        Assertions.assertThat(shortUrlRepository.findAllByOwnerId(ownerA.getId()))
                .extracting(ShortUrl::getShortCode)
                .containsExactly("jpa00a");
    }

    @Test
    void reportsWhetherShortCodeExists() {
        User owner = persistOwner("jpa-test-owner-3@example.com");
        shortUrlRepository.save(ShortUrl.builder()
                .shortCode("jpa003")
                .originalUrl("https://example.com/jpa-exists")
                .owner(owner)
                .build());

        Assertions.assertThat(shortUrlRepository.existsByShortCode("jpa003")).isTrue();
        Assertions.assertThat(shortUrlRepository.existsByShortCode("jpa999")).isFalse();
    }
}
