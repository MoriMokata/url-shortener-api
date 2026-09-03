package com.example.urlshortener.controller;

import com.example.urlshortener.dto.ShortenUrlRequest;
import com.example.urlshortener.dto.ShortenUrlResponse;
import com.example.urlshortener.security.AuthenticatedUser;
import com.example.urlshortener.service.ShortUrlService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class ShortUrlController {

    private final ShortUrlService shortUrlService;

    @PostMapping("/shorten")
    @ResponseStatus(HttpStatus.CREATED)
    public ShortenUrlResponse shorten(
            @Valid @RequestBody ShortenUrlRequest request,
            @AuthenticationPrincipal AuthenticatedUser principal) {
        return shortUrlService.createShortUrl(request, principal.id());
    }
}
