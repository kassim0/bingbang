package com.stars.bigbang.controller;

import com.stars.bigbang.dto.payload.RegisterRequestDto;
import com.stars.bigbang.service.AuthService;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("auth")
@AllArgsConstructor
public class AuthController {

    private AuthService authService;

    @PostMapping(value = "/signIn")
    public ResponseEntity<String> signIn(@RequestBody RegisterRequestDto registerRequestDto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(authService.createAccount(registerRequestDto));
    }
}
