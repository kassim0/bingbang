package com.stars.bigbang.service;

import com.stars.bigbang.dto.record.LoginRequestDto;
import com.stars.bigbang.dto.record.RegisterRequestDto;
import com.stars.bigbang.dto.response.AuthResponseDto;
import com.stars.bigbang.entity.User;
import com.stars.bigbang.repository.UserRepository;
import com.stars.bigbang.security.JwtService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private AuthenticationManager authenticationManager;
    @Mock
    private JwtService jwtService;

    private AuthService authService;

    private AuthService service() {
        return new AuthService(userRepository, passwordEncoder, authenticationManager, jwtService);
    }

    @Test
    void registerGuestCreatesGuestUserAndReturnsGuestToken() {
        authService = service();
        when(passwordEncoder.encode(any())).thenReturn("hashed-random-password");
        when(jwtService.generateGuestToken(any())).thenReturn("guest-jwt-token");

        AuthResponseDto response = authService.registerGuest();

        ArgumentCaptor<User> savedUser = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(savedUser.capture());
        assertTrue(savedUser.getValue().isGuest());
        assertEquals("guest-jwt-token", response.token());
        assertTrue(response.user().guest());
        verify(jwtService, never()).generateToken(any());
    }

    @Test
    void registerHashesPasswordAndReturnsToken() {
        authService = service();
        RegisterRequestDto request = new RegisterRequestDto("alice", "alice@example.com", "password123");
        when(userRepository.existsByEmail("alice@example.com")).thenReturn(false);
        when(userRepository.existsByUsername("alice")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("hashed-password");
        when(jwtService.generateToken(any())).thenReturn("jwt-token");

        AuthResponseDto response = authService.register(request);

        ArgumentCaptor<User> savedUser = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(savedUser.capture());
        assertEquals("hashed-password", savedUser.getValue().getPassword());
        assertEquals("jwt-token", response.token());
        assertEquals("alice@example.com", response.user().email());
    }

    @Test
    void registerRejectsDuplicateEmail() {
        authService = service();
        RegisterRequestDto request = new RegisterRequestDto("alice", "alice@example.com", "password123");
        when(userRepository.existsByEmail("alice@example.com")).thenReturn(true);

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () -> authService.register(request));
        assertEquals(409, ex.getStatusCode().value());
        verify(userRepository, never()).save(any());
    }

    @Test
    void loginReturnsTokenOnValidCredentials() {
        authService = service();
        LoginRequestDto request = new LoginRequestDto("alice@example.com", "password123");
        User user = new User("alice", "alice@example.com", "hashed-password");
        when(userRepository.findByEmail("alice@example.com")).thenReturn(Optional.of(user));
        when(jwtService.generateToken(any())).thenReturn("jwt-token");

        AuthResponseDto response = authService.login(request);

        assertEquals("jwt-token", response.token());
        assertEquals("alice", response.user().username());
    }

    @Test
    void loginRejectsBadCredentials() {
        authService = service();
        LoginRequestDto request = new LoginRequestDto("alice@example.com", "wrong-password");
        doThrow(new BadCredentialsException("bad")).when(authenticationManager).authenticate(any());

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () -> authService.login(request));
        assertEquals(401, ex.getStatusCode().value());
    }
}
