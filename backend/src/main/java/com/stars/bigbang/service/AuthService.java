package com.stars.bigbang.service;

import com.stars.bigbang.dto.record.LoginRequestDto;
import com.stars.bigbang.dto.record.RegisterRequestDto;
import com.stars.bigbang.dto.response.AuthResponseDto;
import com.stars.bigbang.dto.response.UserDto;
import com.stars.bigbang.entity.User;
import com.stars.bigbang.repository.UserRepository;
import com.stars.bigbang.security.JwtService;
import com.stars.bigbang.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    /**
     * Crée un compte invité (sans email/mot de passe choisis par le visiteur) pour lui permettre
     * de créer des GamesList sans inscription, retrouvées tant qu'il garde son token (localStorage).
     */
    public AuthResponseDto registerGuest() {
        String suffix = UUID.randomUUID().toString().replace("-", "").substring(0, 8);
        User user = new User(
                "Invité-" + suffix,
                "guest-" + UUID.randomUUID() + "@bingbang.local",
                passwordEncoder.encode(UUID.randomUUID().toString())
        );
        user.setGuest(true);
        userRepository.save(user);

        String token = jwtService.generateGuestToken(new UserPrincipal(user));
        return new AuthResponseDto(token, UserDto.from(user));
    }

    public AuthResponseDto register(RegisterRequestDto request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email already in use");
        }
        if (userRepository.existsByUsername(request.username())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Username already in use");
        }

        User user = new User(request.username(), request.email(), passwordEncoder.encode(request.password()));
        userRepository.save(user);

        String token = jwtService.generateToken(new UserPrincipal(user));
        return new AuthResponseDto(token, UserDto.from(user));
    }

    public AuthResponseDto login(LoginRequestDto request) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.email(), request.password())
            );
        } catch (BadCredentialsException e) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid email or password");
        }

        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid email or password"));

        String token = jwtService.generateToken(new UserPrincipal(user));
        return new AuthResponseDto(token, UserDto.from(user));
    }
}
