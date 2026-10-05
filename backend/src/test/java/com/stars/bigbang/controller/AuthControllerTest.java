package com.stars.bigbang.controller;

import com.stars.bigbang.dto.payload.RegisterRequestDto;
import com.stars.bigbang.entity.User;
import com.stars.bigbang.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class AuthControllerTest {

    @Autowired
    private TestRestTemplate restTemplate;
    @Autowired
    private UserRepository userRepository;
    private static final String API_CREATE_ACCOUNT = "/auth/signIn";

    @Test
    public void createAccountTest_cas_nominal(){
        RegisterRequestDto registerRequestDto = new RegisterRequestDto("test","test@test.com","test");
        ResponseEntity<String> response = restTemplate.postForEntity(API_CREATE_ACCOUNT, registerRequestDto, String.class);
        assertEquals(HttpStatus.CREATED,response.getStatusCode());
        User newUser = userRepository.findByEmail("test@test.com").orElseThrow();
        assertEquals("test", newUser.getUsername());
        assertNotEquals("test", newUser.getPassword());
    }
}
