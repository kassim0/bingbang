package com.stars.bigbang.rest;


import com.stars.bigbang.controller.rest.RawgApiRestController;
import com.stars.bigbang.dto.RawgDto.RawgResponseDto;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes=RawgApiRestController.class)
@SpringBootTest
public class RawgApiTest {

    private RawgApiRestController rawgApi;

    @Test
    void methodeTest(){
        RawgResponseDto rawgResponseDto = rawgApi.searchGamesByName("zelda","d4df6345d7fb4a4e842849ef2bf16ba7");
        System.out.println("réponse "+rawgResponseDto);
    }
}
