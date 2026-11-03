package com.stars.bigbang.service;

import com.stars.bigbang.dto.payload.LoginRequestDto;
import com.stars.bigbang.dto.payload.RegisterRequestDto;
import com.stars.bigbang.dto.response.AuthResponseDto;
import com.stars.bigbang.dto.response.UserDto;
import com.stars.bigbang.entity.User;
import com.stars.bigbang.repository.UserRepository;
import com.stars.bigbang.security.JwtService;
import com.stars.bigbang.security.UserPrincipal;
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
        User newUser = createUser(registerRequestDto);
        return generateAuthReponse(newUser);
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

        return generateAuthReponse(user.get());
    }

    public UserDto getMe(UserPrincipal principal) {
        return new UserDto(principal.getEmail(),principal.getDisplayName());
    }

    private User createUser(RegisterRequestDto registerRequestDto) {
        String email = normalizeEmail(registerRequestDto.email());
        if(userRepository.existsByEmail(email))
            throw new ResponseStatusException(HttpStatus.CONFLICT,"Email already in use");

        User user = new User();
        user.setUsername(registerRequestDto.username());
        user.setPassword(passwordEncoder.encode(registerRequestDto.password()));
        user.setEmail(email);
        userRepository.saveAndFlush(user);

        return user;
    }

    private AuthResponseDto generateAuthReponse(User user){
        String token = jwtService.generateToken(user.getEmail());
        UserDto userDto = new UserDto(user.getEmail(), user.getUsername());

        return new AuthResponseDto(token, userDto);
    }
}
