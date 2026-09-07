package com.stars.bigbang.controller;

import com.stars.bigbang.dto.rawgDto.RawgResultsDto;
import com.stars.bigbang.dto.record.NewGamesListDto;
import com.stars.bigbang.dto.record.UpdateGamesListDto;
import com.stars.bigbang.dto.response.GameDto;
import com.stars.bigbang.dto.response.GamesListDto;
import com.stars.bigbang.service.GameService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@AllArgsConstructor
@Validated
public class JeuxRestController {

    private GameService gameService;

    @PutMapping(value = "/newGamesList")
    public ResponseEntity<GamesListDto> saveListGames(@RequestBody NewGamesListDto newGamesListDto) {
        return ResponseEntity.ok(GamesListDto.from(gameService.createGamesList(newGamesListDto.name(), newGamesListDto.rawgGames())));
    }

    @GetMapping(value = "/getGamesLists")
    public ResponseEntity<List<GamesListDto>> getListGames() {
        return ResponseEntity.ok(gameService.getListGames());
    }

    @DeleteMapping(value = "/updateGamesList")
    public void updateGamesList(@RequestBody UpdateGamesListDto updateGamesListDto){
        gameService.updateGamesList(updateGamesListDto);
    }
}
