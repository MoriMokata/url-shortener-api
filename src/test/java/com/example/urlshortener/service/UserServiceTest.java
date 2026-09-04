package com.example.urlshortener.service;

import java.time.Instant;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.example.urlshortener.dto.RegisterRequest;
import com.example.urlshortener.dto.RegisterResponse;
import com.example.urlshortener.entity.User;
import com.example.urlshortener.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    private UserService userService;

    @BeforeEach
    void setUp() {
        userService = new UserService(userRepository, passwordEncoder);
    }

    @Test
    void registersUserWithHashedPassword() {
        RegisterRequest request = new RegisterRequest("new-user@example.com", "P@ssw0rd");

        Mockito.when(userRepository.existsByEmail("new-user@example.com")).thenReturn(false);
        Mockito.when(passwordEncoder.encode("P@ssw0rd")).thenReturn("bcrypt-hashed-value");
        Mockito.when(userRepository.save(ArgumentMatchers.any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            user.setId(1L);
            user.setCreatedAt(Instant.parse("2026-01-01T00:00:00Z"));
            return user;
        });

        RegisterResponse response = userService.register(request);

        ArgumentCaptor<User> savedUserCaptor = ArgumentCaptor.forClass(User.class);
        org.mockito.Mockito.verify(userRepository).save(savedUserCaptor.capture());

        Assertions.assertThat(savedUserCaptor.getValue().getPassword()).isEqualTo("bcrypt-hashed-value");
        Assertions.assertThat(savedUserCaptor.getValue().getPassword()).isNotEqualTo("P@ssw0rd");
        Assertions.assertThat(response.id()).isEqualTo(1L);
        Assertions.assertThat(response.email()).isEqualTo("new-user@example.com");
    }
}
