package com.example.urlshortener.dto;

import java.time.Instant;

public record ShortUrlSummaryResponse(
        Long id,
        String shortCode,
        String shortUrl,
        String originalUrl,
        boolean isActive,
        Instant createdAt) {
}
