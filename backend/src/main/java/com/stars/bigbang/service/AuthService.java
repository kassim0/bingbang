package com.stars.bigbang.service;

import com.stars.bigbang.dto.payload.RegisterRequestDto;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    public String createAccount( RegisterRequestDto registerRequestDto){
        return "success";
    }
}
