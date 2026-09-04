package com.example.urlshortener.service;

import com.example.urlshortener.dto.ShortenUrlRequest;
import com.example.urlshortener.dto.ShortenUrlResponse;
import com.example.urlshortener.dto.ShortUrlSummaryResponse;
import com.example.urlshortener.entity.ShortUrl;
import com.example.urlshortener.entity.User;
import com.example.urlshortener.exception.ShortUrlAccessDeniedException;
import com.example.urlshortener.exception.ShortUrlNotFoundException;
import com.example.urlshortener.repository.ShortUrlRepository;
import com.example.urlshortener.repository.UserRepository;
import com.example.urlshortener.service.shortcode.ShortCodeGenerator;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

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

        Mockito.when(userRepository.getReferenceById(1L)).thenReturn(owner);
        Mockito.when(shortCodeGenerator.generate()).thenReturn("abc123");
        Mockito.when(shortUrlRepository.existsByShortCode("abc123")).thenReturn(false);
        Mockito.when(shortUrlRepository.save(ArgumentMatchers.any(ShortUrl.class))).thenAnswer(invocation -> {
            ShortUrl saved = invocation.getArgument(0);
            saved.setId(10L);
            saved.setCreatedAt(Instant.parse("2026-01-01T00:00:00Z"));
            return saved;
        });

        ShortenUrlResponse response = shortUrlService.createShortUrl(request, 1L);

        Assertions.assertThat(response.id()).isEqualTo(10L);
        Assertions.assertThat(response.shortCode()).isEqualTo("abc123");
        Assertions.assertThat(response.shortUrl()).isEqualTo("http://localhost:8080/abc123");
        Assertions.assertThat(response.originalUrl()).isEqualTo("https://example.com/some/very/long/link");
        Assertions.assertThat(response.createdAt()).isEqualTo(Instant.parse("2026-01-01T00:00:00Z"));
    }

    @Test
    void retriesGenerationWhenShortCodeCollides() {
        User owner = User.builder().id(1L).build();
        ShortenUrlRequest request = new ShortenUrlRequest("https://example.com/collision-case");

        Mockito.when(userRepository.getReferenceById(1L)).thenReturn(owner);
        Mockito.when(shortCodeGenerator.generate()).thenReturn("dup001", "uniq02");
        Mockito.when(shortUrlRepository.existsByShortCode("dup001")).thenReturn(true);
        Mockito.when(shortUrlRepository.existsByShortCode("uniq02")).thenReturn(false);
        Mockito.when(shortUrlRepository.save(ArgumentMatchers.any(ShortUrl.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        ShortenUrlResponse response = shortUrlService.createShortUrl(request, 1L);

        Assertions.assertThat(response.shortCode()).isEqualTo("uniq02");
        Mockito.verify(shortCodeGenerator, Mockito.times(2)).generate();
    }

    @Test
    void resolvesActiveShortUrlToOriginalUrl() {
        ShortUrl shortUrl = ShortUrl.builder()
                .shortCode("abc123")
                .originalUrl("https://example.com/target")
                .active(true)
                .build();

        Mockito.when(shortUrlRepository.findByShortCodeAndActiveTrue("abc123")).thenReturn(Optional.of(shortUrl));

        Assertions.assertThat(shortUrlService.resolve("abc123")).isEqualTo("https://example.com/target");
    }

    @Test
    void throwsNotFoundForMissingShortCode() {
        Mockito.when(shortUrlRepository.findByShortCodeAndActiveTrue("missing")).thenReturn(Optional.empty());

        Assertions.assertThatThrownBy(() -> shortUrlService.resolve("missing"))
                .isInstanceOf(ShortUrlNotFoundException.class);
    }

    @Test
    void throwsNotFoundForDeactivatedShortCode() {
        Mockito.when(shortUrlRepository.findByShortCodeAndActiveTrue("deactivated")).thenReturn(Optional.empty());

        Assertions.assertThatThrownBy(() -> shortUrlService.resolve("deactivated"))
                .isInstanceOf(ShortUrlNotFoundException.class);
    }

    @Test
    void listsOnlyShortUrlsOwnedByGivenOwner() {
        User owner = User.builder().id(1L).build();
        ShortUrl ownUrl = ShortUrl.builder()
                .id(100L)
                .shortCode("own001")
                .originalUrl("https://example.com/own")
                .owner(owner)
                .active(true)
                .createdAt(Instant.parse("2026-01-01T00:00:00Z"))
                .build();

        Mockito.when(shortUrlRepository.findAllByOwnerId(1L)).thenReturn(List.of(ownUrl));

        List<ShortUrlSummaryResponse> result = shortUrlService.listByOwner(1L);

        Assertions.assertThat(result).hasSize(1);
        Assertions.assertThat(result.get(0).id()).isEqualTo(100L);
        Assertions.assertThat(result.get(0).shortCode()).isEqualTo("own001");
        Assertions.assertThat(result.get(0).shortUrl()).isEqualTo("http://localhost:8080/own001");
        Assertions.assertThat(result.get(0).isActive()).isTrue();
    }

    @Test
    void deactivatesShortUrlWhenCallerIsOwner() {
        User owner = User.builder().id(1L).build();
        ShortUrl shortUrl = ShortUrl.builder()
                .id(100L)
                .shortCode("own001")
                .originalUrl("https://example.com/own")
                .owner(owner)
                .active(true)
                .build();

        Mockito.when(shortUrlRepository.findById(100L)).thenReturn(Optional.of(shortUrl));

        shortUrlService.deactivate(100L, 1L);

        Assertions.assertThat(shortUrl.isActive()).isFalse();
    }

    @Test
    void rejectsDeactivationWhenCallerIsNotOwner() {
        User owner = User.builder().id(1L).build();
        ShortUrl shortUrl = ShortUrl.builder()
                .id(100L)
                .shortCode("own001")
                .originalUrl("https://example.com/own")
                .owner(owner)
                .active(true)
                .build();

        Mockito.when(shortUrlRepository.findById(100L)).thenReturn(Optional.of(shortUrl));

        Assertions.assertThatThrownBy(() -> shortUrlService.deactivate(100L, 2L))
                .isInstanceOf(ShortUrlAccessDeniedException.class);
        Assertions.assertThat(shortUrl.isActive()).isTrue();
    }

    @Test
    void throwsNotFoundWhenDeactivatingMissingShortUrl() {
        Mockito.when(shortUrlRepository.findById(999L)).thenReturn(Optional.empty());

        Assertions.assertThatThrownBy(() -> shortUrlService.deactivate(999L, 1L))
                .isInstanceOf(ShortUrlNotFoundException.class);
    }
}
