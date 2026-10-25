package com.stars.bigbang.controller;

import com.stars.bigbang.dto.payload.LoginRequestDto;
import com.stars.bigbang.dto.payload.RegisterRequestDto;
import com.stars.bigbang.dto.response.AuthResponseDto;
import com.stars.bigbang.dto.response.UserDto;
import com.stars.bigbang.security.UserPrincipal;
import com.stars.bigbang.service.AuthService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("api/auth")
@AllArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping(value = "/register")
    public ResponseEntity<AuthResponseDto> register(@Valid @RequestBody RegisterRequestDto registerRequestDto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(authService.createAccount(registerRequestDto));
    }

    @PostMapping(value = "/login")
    public ResponseEntity<AuthResponseDto> login(@Valid @RequestBody LoginRequestDto loginRequestDto) {
        return ResponseEntity.ok(authService.login(loginRequestDto));
    }

    @GetMapping(value = "/me")
    public ResponseEntity<UserDto> me(@AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(authService.getMe(principal));
    }
}
