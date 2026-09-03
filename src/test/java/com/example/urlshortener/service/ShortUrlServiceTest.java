package com.example.urlshortener.service;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.Mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import com.example.urlshortener.dto.ShortenUrlRequest;
import com.example.urlshortener.dto.ShortenUrlResponse;
import com.example.urlshortener.entity.ShortUrl;
import com.example.urlshortener.entity.User;
import com.example.urlshortener.repository.ShortUrlRepository;
import com.example.urlshortener.repository.UserRepository;
import com.example.urlshortener.service.shortcode.ShortCodeGenerator;

@ExtendWith(MockitoExtension.class)
class ShortUrlServiceTest {

    private static final String BASE_URL = "http://localhost:8080";

    @Mock
    private ShortUrlRepository shortUrlRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ShortCodeGenerator shortCodeGenerator;

    private ShortUrlService shortUrlService;

    @BeforeEach
    void setUp() {
        shortUrlService = new ShortUrlService(shortUrlRepository, userRepository, shortCodeGenerator, BASE_URL);
    }

    @Test
    void createsShortUrlAndReturnsResponseMatchingContract() {
        User owner = User.builder().id(1L).build();
        ShortenUrlRequest request = new ShortenUrlRequest("https://example.com/some/very/long/link");

        when(userRepository.getReferenceById(1L)).thenReturn(owner);
        when(shortCodeGenerator.generate()).thenReturn("abc123");
        when(shortUrlRepository.existsByShortCode("abc123")).thenReturn(false);
        when(shortUrlRepository.save(any(ShortUrl.class))).thenAnswer(invocation -> {
            ShortUrl saved = invocation.getArgument(0);
            saved.setId(10L);
            saved.setCreatedAt(Instant.parse("2026-01-01T00:00:00Z"));
            return saved;
        });

        ShortenUrlResponse response = shortUrlService.createShortUrl(request, 1L);

        assertThat(response.id()).isEqualTo(10L);
        assertThat(response.shortCode()).isEqualTo("abc123");
        assertThat(response.shortUrl()).isEqualTo("http://localhost:8080/r/abc123");
        assertThat(response.originalUrl()).isEqualTo("https://example.com/some/very/long/link");
        assertThat(response.createdAt()).isEqualTo(Instant.parse("2026-01-01T00:00:00Z"));
    }

    @Test
    void retriesGenerationWhenShortCodeCollides() {
        User owner = User.builder().id(1L).build();
        ShortenUrlRequest request = new ShortenUrlRequest("https://example.com/collision-case");

        when(userRepository.getReferenceById(1L)).thenReturn(owner);
        when(shortCodeGenerator.generate()).thenReturn("dup001", "uniq02");
        when(shortUrlRepository.existsByShortCode("dup001")).thenReturn(true);
        when(shortUrlRepository.existsByShortCode("uniq02")).thenReturn(false);
        when(shortUrlRepository.save(any(ShortUrl.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ShortenUrlResponse response = shortUrlService.createShortUrl(request, 1L);

        assertThat(response.shortCode()).isEqualTo("uniq02");
        verify(shortCodeGenerator, times(2)).generate();
    }
}
