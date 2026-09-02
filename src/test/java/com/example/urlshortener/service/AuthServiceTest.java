package com.example.urlshortener.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import com.example.urlshortener.dto.LoginRequest;
import com.example.urlshortener.dto.LoginResponse;
import com.example.urlshortener.entity.User;
import com.example.urlshortener.exception.InvalidCredentialsException;
import com.example.urlshortener.repository.UserRepository;
import com.example.urlshortener.security.JwtService;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    private AuthService authService;

    @BeforeEach
    void setUp() {
        authService = new AuthService(userRepository, passwordEncoder, jwtService);
    }

    @Test
    void loginSucceedsAndReturnsToken() {
        User user = User.builder().id(1L).email("user@example.com").password("hashed").build();
        LoginRequest request = new LoginRequest("user@example.com", "P@ssw0rd");

        when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("P@ssw0rd", "hashed")).thenReturn(true);
        when(jwtService.generateToken(1L, "user@example.com")).thenReturn("signed-jwt-token");
        when(jwtService.getExpirationSeconds()).thenReturn(86400L);

        LoginResponse response = authService.login(request);

        assertThat(response.token()).isEqualTo("signed-jwt-token");
        assertThat(response.expiresIn()).isEqualTo(86400L);
    }

    @Test
    void rejectsLoginWithWrongPassword() {
        User user = User.builder().id(1L).email("user@example.com").password("hashed").build();
        LoginRequest request = new LoginRequest("user@example.com", "wrong-password");

        when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrong-password", "hashed")).thenReturn(false);

        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(InvalidCredentialsException.class)
                .hasMessage("Invalid email or password");
    }

    @Test
    void rejectsLoginForNonExistingUserWithSameMessageAsWrongPassword() {
        LoginRequest request = new LoginRequest("missing@example.com", "P@ssw0rd");

        when(userRepository.findByEmail("missing@example.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(InvalidCredentialsException.class)
                .hasMessage("Invalid email or password");
    }
}
