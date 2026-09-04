package com.example.urlshortener.dto;

import java.util.stream.Stream;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class ValidUrlValidatorTest {

    private final ValidUrlValidator validator = new ValidUrlValidator();

    static Stream<Arguments> validUrls() {
        return Stream.of(
                Arguments.arguments("https://example.com"),
                Arguments.arguments("http://example.com/some/very/long/link"),
                Arguments.arguments("https://sub.example.com:8443/path?query=1"));
    }

    static Stream<Arguments> invalidUrls() {
        return Stream.of(
                Arguments.arguments((Object) null),
                Arguments.arguments(""),
                Arguments.arguments("   "),
                Arguments.arguments("not-a-url"),
                Arguments.arguments("ftp://example.com/file"),
                Arguments.arguments("example.com"));
    }

    @ParameterizedTest
    @MethodSource("validUrls")
    void acceptsValidHttpAndHttpsUrls(String url) {
        Assertions.assertThat(validator.isValid(url, null)).isTrue();
    }

    @ParameterizedTest
    @MethodSource("invalidUrls")
    void rejectsNonUrlOrNonHttpValues(String url) {
        Assertions.assertThat(validator.isValid(url, null)).isFalse();
    }
}
