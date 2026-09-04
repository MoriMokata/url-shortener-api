package com.example.urlshortener.exception;

public class ShortCodeGenerationException extends RuntimeException {

    public ShortCodeGenerationException() {
        super("Failed to generate a unique short code, please retry");
    }
}
