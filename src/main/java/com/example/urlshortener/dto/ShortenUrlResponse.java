package com.example.urlshortener.dto;

import java.time.Instant;

public record ShortenUrlResponse(
        Long id,
        String shortCode,
        String shortUrl,
        String originalUrl,
        Instant createdAt) {
}
