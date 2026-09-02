package com.example.urlshortener.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import org.junit.jupiter.api.Test;

import io.jsonwebtoken.ExpiredJwtException;

class JwtServiceTest {

    private final JwtService jwtService =
            new JwtService("test-secret-key-with-enough-length-for-hs256-signing", 60_000);

    @Test
    void generatesTokenAndExtractsClaims() {
        String token = jwtService.generateToken(42L, "user@example.com");

        assertThat(jwtService.extractUserId(token)).isEqualTo(42L);
        assertThat(jwtService.extractEmail(token)).isEqualTo("user@example.com");
    }


    @Test
    void rejectsExpiredToken() {
        JwtService expiringJwtService =
                new JwtService("test-secret-key-with-enough-length-for-hs256-signing", -1_000);
        String token = expiringJwtService.generateToken(1L, "user@example.com");

        assertThatThrownBy(() -> expiringJwtService.parseToken(token))
                .isInstanceOf(ExpiredJwtException.class);
    }
}
