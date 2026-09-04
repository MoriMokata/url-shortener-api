package com.example.urlshortener.service.shortcode;

import java.security.SecureRandom;
import org.springframework.stereotype.Component;

@Component
public class RandomBase62ShortCodeGenerator implements ShortCodeGenerator {

    private static final String CHARSET =
            "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
    private static final int CODE_LENGTH = 6;

    private final SecureRandom secureRandom = new SecureRandom();

    @Override
    public String generate() {
        StringBuilder code = new StringBuilder(CODE_LENGTH);
        for (int i = 0; i < CODE_LENGTH; i++) {
            code.append(CHARSET.charAt(secureRandom.nextInt(CHARSET.length())));
        }
        return code.toString();
    }
}
