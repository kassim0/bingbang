package com.stars.bigbang.controller;

import com.stars.bigbang.dto.RawgDto.RawgResponseDto;
import com.stars.bigbang.rest.RawgApi;
import lombok.AllArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/games")
@AllArgsConstructor
@Validated
public class JeuxRestController {

    private RawgApi rawgApi;

    @GetMapping()
    public RawgResponseDto getJeuxZelda(){
        return rawgApi.searchGamesByName("the legend of zelda","d4df6345d7fb4a4e842849ef2bf16ba7");
    }

    @GetMapping("/{gameName}")
    public RawgResponseDto getJeuxByName(@PathVariable String gameName){
        return rawgApi.searchGamesByName(gameName,"d4df6345d7fb4a4e842849ef2bf16ba7");
    }
}
