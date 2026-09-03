package com.example.urlshortener.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.urlshortener.dto.ShortenUrlRequest;
import com.example.urlshortener.dto.ShortenUrlResponse;
import com.example.urlshortener.entity.ShortUrl;
import com.example.urlshortener.exception.ShortCodeGenerationException;
import com.example.urlshortener.exception.ShortUrlNotFoundException;
import com.example.urlshortener.repository.ShortUrlRepository;
import com.example.urlshortener.repository.UserRepository;
import com.example.urlshortener.service.shortcode.ShortCodeGenerator;

@Service
public class ShortUrlService {

    private static final int MAX_GENERATION_ATTEMPTS = 5;

    private final ShortUrlRepository shortUrlRepository;
    private final UserRepository userRepository;
    private final ShortCodeGenerator shortCodeGenerator;
    private final String baseUrl;

    public ShortUrlService(
            ShortUrlRepository shortUrlRepository,
            UserRepository userRepository,
            ShortCodeGenerator shortCodeGenerator,
            @Value("${app.base-url}") String baseUrl) {
        this.shortUrlRepository = shortUrlRepository;
        this.userRepository = userRepository;
        this.shortCodeGenerator = shortCodeGenerator;
        this.baseUrl = baseUrl;
    }

    @Transactional
    public ShortenUrlResponse createShortUrl(ShortenUrlRequest request, Long ownerId) {
        String shortCode = generateUniqueShortCode();

        ShortUrl shortUrl = ShortUrl.builder()
                .shortCode(shortCode)
                .originalUrl(request.originalUrl())
                .owner(userRepository.getReferenceById(ownerId))
                .build();

        ShortUrl saved = shortUrlRepository.save(shortUrl);

        return toResponse(saved);
    }

    public String resolve(String shortCode) {
        return shortUrlRepository.findByShortCodeAndActiveTrue(shortCode)
                .map(ShortUrl::getOriginalUrl)
                .orElseThrow(ShortUrlNotFoundException::new);
    }

    private String generateUniqueShortCode() {
        for (int attempt = 0; attempt < MAX_GENERATION_ATTEMPTS; attempt++) {
            String candidate = shortCodeGenerator.generate();
            if (!shortUrlRepository.existsByShortCode(candidate)) {
                return candidate;
            }
        }
        throw new ShortCodeGenerationException();
    }

    private ShortenUrlResponse toResponse(ShortUrl shortUrl) {
        return new ShortenUrlResponse(
                shortUrl.getId(),
                shortUrl.getShortCode(),
                baseUrl + "/" + shortUrl.getShortCode(),
                shortUrl.getOriginalUrl(),
                shortUrl.getCreatedAt());
    }
}
