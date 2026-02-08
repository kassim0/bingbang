package com.stars.bigbang.controller;

import com.stars.bigbang.dto.RawgDto.RawgResponseDto;
import com.stars.bigbang.dto.RawgDto.RawgResultsDto;
import com.stars.bigbang.entity.Game;
import com.stars.bigbang.rest.RawgApi;
import com.stars.bigbang.service.GameService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/games")
@AllArgsConstructor
@Validated
public class JeuxRestController {

    private RawgApi rawgApi;
    private GameService gameService;

    @GetMapping(produces="application/json")
    public RawgResponseDto getJeuxZelda(){
        return rawgApi.searchGamesByName("the legend of zelda","d4df6345d7fb4a4e842849ef2bf16ba7");
    }

    @GetMapping(value="/{gameName}", produces="application/json")
    public RawgResponseDto getJeuxByName(@PathVariable String gameName){
        return rawgApi.searchGamesByName(gameName,"d4df6345d7fb4a4e842849ef2bf16ba7");
    }

    @PostMapping(consumes="application/json", produces="application/json")
    public ResponseEntity<Game> saveGame(@RequestBody RawgResultsDto gameDto) {
        Game game = new Game();
        game.setName(gameDto.getName());
        game.setSlug(gameDto.getSlug());
        game.setBackgroundImage(gameDto.getBackground_image());
        game.setRawgId(gameDto.getId());
        return ResponseEntity.ok(gameService.save(game));
    }
}
