package com.stars.bigbang.rest;


import com.stars.bigbang.dto.RawgResponseDto;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertEquals;

/*@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes=RawgApi.class)*/
@SpringBootTest
public class RawgApiTest {

    @Autowired
    private RawgApi rawgApi;

    @Test
    void methodeTest(){
        RawgResponseDto rawgResponseDto = rawgApi.searchGamesByName("The Legend of Zelda","d4df6345d7fb4a4e842849ef2bf16ba7");
    }
}
