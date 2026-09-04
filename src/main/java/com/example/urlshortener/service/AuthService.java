package com.example.urlshortener.service;

import com.example.urlshortener.dto.LoginRequest;
import com.example.urlshortener.dto.LoginResponse;
import com.example.urlshortener.entity.User;
import com.example.urlshortener.exception.InvalidCredentialsException;
import com.example.urlshortener.repository.UserRepository;
import com.example.urlshortener.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public LoginResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.email())
                .orElseThrow(InvalidCredentialsException::new);

        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
            throw new InvalidCredentialsException();
        }

        String token = jwtService.generateToken(user.getId(), user.getEmail());
        return new LoginResponse(token, jwtService.getExpirationSeconds());
    }
}
