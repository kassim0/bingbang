package com.stars.bigbang.service;

import com.stars.bigbang.dto.payload.RegisterRequestDto;
import com.stars.bigbang.entity.User;
import com.stars.bigbang.repository.UserRepository;
import lombok.AllArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class AuthService {

    UserRepository userRepository;
    PasswordEncoder passwordEncoder;

    public String createAccount( RegisterRequestDto registerRequestDto){
        User user = new User();
        user.setUsername(registerRequestDto.username());
        user.setPassword(passwordEncoder.encode(registerRequestDto.password()));
        user.setEmail(registerRequestDto.email());
        userRepository.save(user);
        return "success";
    }
}
