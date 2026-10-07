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

    @Test
    public void createAccountTest_cas_email_existant(){
        RegisterRequestDto registerRequestDto1 = new RegisterRequestDto("test1","test@test.com","test1");
        RegisterRequestDto registerRequestDto2 = new RegisterRequestDto("test2","test@test.com","test2");
        ResponseEntity<String> response1 = restTemplate.postForEntity(API_CREATE_ACCOUNT, registerRequestDto1, String.class);
        ResponseEntity<String> response2 = restTemplate.postForEntity(API_CREATE_ACCOUNT, registerRequestDto2, String.class);
        assertEquals(HttpStatus.CREATED,response1.getStatusCode());
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR,response2.getStatusCode());
    }
}
