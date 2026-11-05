package com.stars.bigbang.rest;


import com.stars.bigbang.dto.rawgDto.RawgResponseDto;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;

@EnabledIfEnvironmentVariable(named = "RAWG_API_KEY", matches = ".+")
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes=RawgApi.class)
@SpringBootTest
public class RawgApiTest {

    @Autowired
    private RawgApi rawgApi;

    @Test
    void methodeTest(){
        RawgResponseDto rawgResponseDto = rawgApi.searchGamesByName("The Legend of Zelda", System.getenv("RAWG_API_KEY"));
        rawgResponseDto.getResults().forEach(result->{
            System.out.println("name: "+result.name());
        });
    }
}
