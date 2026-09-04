package com.example.urlshortener.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;

public record ShortenUrlRequest(
        @NotBlank @ValidUrl @JsonProperty("original_url") String originalUrl) {
}
