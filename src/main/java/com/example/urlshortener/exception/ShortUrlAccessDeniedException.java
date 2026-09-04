package com.example.urlshortener.exception;

public class ShortUrlAccessDeniedException extends RuntimeException {

    public ShortUrlAccessDeniedException() {
        super("You do not have permission to modify this short URL");
    }
}
