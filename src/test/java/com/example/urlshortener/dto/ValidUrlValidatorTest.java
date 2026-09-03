package com.example.urlshortener.dto;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.params.provider.Arguments.arguments;

import java.util.stream.Stream;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class ValidUrlValidatorTest {

    private final ValidUrlValidator validator = new ValidUrlValidator();

    static Stream<Arguments> validUrls() {
        return Stream.of(
                arguments("https://example.com"),
                arguments("http://example.com/some/very/long/link"),
                arguments("https://sub.example.com:8443/path?query=1"));
    }

    static Stream<Arguments> invalidUrls() {
        return Stream.of(
                arguments((Object) null),
                arguments(""),
                arguments("   "),
                arguments("not-a-url"),
                arguments("ftp://example.com/file"),
                arguments("example.com"));
    }

    @ParameterizedTest
    @MethodSource("validUrls")
    void acceptsValidHttpAndHttpsUrls(String url) {
        assertThat(validator.isValid(url, null)).isTrue();
    }

    @ParameterizedTest
    @MethodSource("invalidUrls")
    void rejectsNonUrlOrNonHttpValues(String url) {
        assertThat(validator.isValid(url, null)).isFalse();
    }
}
