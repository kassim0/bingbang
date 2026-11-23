package com.stars.bigbang.service;

import com.stars.bigbang.dto.payload.LoginRequestDto;
import com.stars.bigbang.dto.payload.RegisterRequestDto;
import com.stars.bigbang.dto.response.AuthResponseDto;
import com.stars.bigbang.dto.response.UserDto;
import com.stars.bigbang.entity.User;
import com.stars.bigbang.repository.GamesListRepository;
import com.stars.bigbang.repository.UserRepository;
import com.stars.bigbang.security.JwtService;
import com.stars.bigbang.security.UserPrincipal;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;
import java.util.UUID;

import static com.stars.bigbang.tools.AuthTools.normalizeEmail;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final String hashFactice;

    private static final String WRONG_AUTH = "wrong auth";
    private static final String ALREADY_CONNECTED = "already connected";
    private final GamesListRepository gamesListRepository;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtService jwtService,
                       GamesListRepository gamesListRepository) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.hashFactice = passwordEncoder.encode("nimportekoi");
        this.gamesListRepository = gamesListRepository;
    }

    @Transactional
    public AuthResponseDto createAccount(RegisterRequestDto registerRequestDto, UserPrincipal userPrincipal) {
        if(userPrincipal != null ){
            if(userPrincipal.isGuest()){
                User guestUser = updateGuestUser(registerRequestDto,userPrincipal.getId());
                return generateAuthReponse(guestUser);
            }
            throw new ResponseStatusException(HttpStatus.CONFLICT, ALREADY_CONNECTED);
        }
        User newUser = createUser(registerRequestDto,false);
        return generateAuthReponse(newUser);
    }

    //Toute cette méthode aurrait pu être remplacer par authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(email, password));
    @Transactional
    public AuthResponseDto login(LoginRequestDto loginRequestDto, UserPrincipal userPrincipal) {
        String email =  normalizeEmail(loginRequestDto.email());
        Optional<User> user = userRepository.findByEmail(email);

        if(user.isEmpty()){
            passwordEncoder.matches(loginRequestDto.password(), hashFactice); // hash factice : temps de réponse identique, que l'email existe ou non.
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, WRONG_AUTH);
        }

        if(!passwordEncoder.matches(loginRequestDto.password(), user.get().getPassword())){
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, WRONG_AUTH);
        }

        if(userPrincipal != null && userPrincipal.isGuest()){
            User guestUser = userRepository.getReferenceById(userPrincipal.getId());
            transferData(guestUser, user.get());
            userRepository.delete(guestUser);
        }

        return generateAuthReponse(user.get());
    }

    /**
     * Déplace toutes les listes de {@code userFrom} vers {@code userTo}, à la suite de celles de {@code userTo}.
     * Les positions sont décalées, pas renumérotées : l'ordre est conservé, un trou éventuel aussi.
     */
    private void transferData(User userFrom, User userTo) {
        int offset = gamesListRepository.findMaxOrder(userTo.getId());
        gamesListRepository.transferAllGamesLists(userFrom, userTo, offset);
    }

    public UserDto getMe(UserPrincipal principal) {

        return new UserDto(principal.getEmail(),principal.getDisplayName(),principal.isGuest());
    }

    private User createUser(RegisterRequestDto registerRequestDto, boolean guest) {
        String email = normalizeEmail(registerRequestDto.email());
        assertUniqueEmail(email);

        User user = new User();
        user.setUsername(registerRequestDto.username());
        user.setPassword(passwordEncoder.encode(registerRequestDto.password()));
        user.setEmail(email);
        user.setGuest(guest);
        userRepository.saveAndFlush(user);

        return user;
    }

    private User updateGuestUser(RegisterRequestDto registerRequestDto, long userId) {
        String email = normalizeEmail(registerRequestDto.email());
        assertUniqueEmail(email);

        User guestUser = userRepository.findById(userId).orElseThrow();
        guestUser.setUsername(registerRequestDto.username());
        guestUser.setEmail(email);
        guestUser.setPassword(passwordEncoder.encode(registerRequestDto.password()));
        guestUser.setGuest(false);
        return userRepository.save(guestUser);
    }

    private void assertUniqueEmail(String email){
        if(userRepository.existsByEmail(email))
            throw new ResponseStatusException(HttpStatus.CONFLICT,"Email already in use");
    }

    private AuthResponseDto generateAuthReponse(User user){
        String token;
        token = user.isGuest() ? jwtService.generateGuestToken(user.getEmail()) : jwtService.generateToken(user.getEmail());
        UserDto userDto = new UserDto(user.getEmail(), user.getUsername(),user.isGuest());

        return new AuthResponseDto(token, userDto);
    }

    public AuthResponseDto generateGuestUser() {
        RegisterRequestDto registerRequestDto = generateRandomAuth();
        User newGuestUser = createUser(registerRequestDto,true);
        return generateAuthReponse(newGuestUser);
    }

    private RegisterRequestDto generateRandomAuth(){
        String username = "guest";
        String email = "guest-"+UUID.randomUUID()+"@bingbang.invalid";
        String password = UUID.randomUUID().toString();
        return new RegisterRequestDto(username,email,password);
    }
}
