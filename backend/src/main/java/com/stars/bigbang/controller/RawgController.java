package com.stars.bigbang.controller;

import com.stars.bigbang.dto.rawgDto.RawgResponseDto;
import com.stars.bigbang.rawg.RawgApi;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/rawg")
@AllArgsConstructor
@Validated
public class RawgController {
    private RawgApi rawgApi;

    @GetMapping(value="/{gameName}", produces="application/json")
    public RawgResponseDto getJeuxByName(@PathVariable String gameName){
        return rawgApi.searchGamesByName(gameName);
    }
}
