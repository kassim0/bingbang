package com.stars.bigbang.service;

import com.stars.bigbang.dto.payload.LoginRequestDto;
import com.stars.bigbang.dto.payload.RegisterRequestDto;
import com.stars.bigbang.dto.response.AuthResponseDto;
import com.stars.bigbang.entity.User;
import com.stars.bigbang.repository.UserRepository;
import com.stars.bigbang.security.JwtService;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

import static com.stars.bigbang.tools.AuthTools.normalizeEmail;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final String hashFactice;

    private static final String WRONG_AUTH = "wrong auth";

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtService jwtService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.hashFactice = passwordEncoder.encode("nimportekoi");
    }

    public AuthResponseDto createAccount(RegisterRequestDto registerRequestDto) {
        String email = normalizeEmail(registerRequestDto.email());
        if(userRepository.existsByEmail(email))
            throw new ResponseStatusException(HttpStatus.CONFLICT,"Email already in use");

        User user = new User();
        user.setUsername(registerRequestDto.username());
        user.setPassword(passwordEncoder.encode(registerRequestDto.password()));
        user.setEmail(email);
        userRepository.save(user);

        return new AuthResponseDto(jwtService.generateToken(user.getEmail()));
    }


    //Toute cette méthode aurrait pu être remplacer par authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(email, password));
    public AuthResponseDto login(LoginRequestDto loginRequestDto) {
        String email =  normalizeEmail(loginRequestDto.email());
        Optional<User> user = userRepository.findByEmail(email);

        if(user.isEmpty()){
            passwordEncoder.matches(loginRequestDto.password(), hashFactice); // hash factice : temps de réponse identique, que l'email existe ou non.
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, WRONG_AUTH);
        }
        if(!passwordEncoder.matches(loginRequestDto.password(), user.get().getPassword())){
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, WRONG_AUTH);
        }

        return new AuthResponseDto(jwtService.generateToken(user.get().getEmail()));
    }
}
