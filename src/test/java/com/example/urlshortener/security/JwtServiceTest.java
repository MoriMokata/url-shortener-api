package com.example.urlshortener.security;

import io.jsonwebtoken.ExpiredJwtException;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

class JwtServiceTest {

    private final JwtService jwtService =
            new JwtService("test-secret-key-with-enough-length-for-hs256-signing", 60_000);

    @Test
    void generatesTokenAndExtractsClaims() {
        String token = jwtService.generateToken(42L, "user@example.com");

        Assertions.assertThat(jwtService.extractUserId(token)).isEqualTo(42L);
        Assertions.assertThat(jwtService.extractEmail(token)).isEqualTo("user@example.com");
    }

    @Test
    void rejectsExpiredToken() {
        JwtService expiringJwtService =
                new JwtService("test-secret-key-with-enough-length-for-hs256-signing", -1_000);
        String token = expiringJwtService.generateToken(1L, "user@example.com");

        Assertions.assertThatThrownBy(() -> expiringJwtService.parseToken(token))
                .isInstanceOf(ExpiredJwtException.class);
    }
}
