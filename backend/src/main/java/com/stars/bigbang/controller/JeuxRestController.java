package com.stars.bigbang.controller;

import com.stars.bigbang.dto.rawgDto.RawgResultsDto;
import com.stars.bigbang.dto.record.NewGamesListDto;
import com.stars.bigbang.dto.record.UpdateGamesListDto;
import com.stars.bigbang.entity.Game;
import com.stars.bigbang.entity.GamesList;
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

    @PostMapping(consumes="application/json", produces="application/json")
    public ResponseEntity<Game> saveGame(@RequestBody RawgResultsDto gameDto) {
        return ResponseEntity.ok(gameService.saveRawgGame(gameDto));
    }

    @PostMapping(value = "/newGamesList")
    public ResponseEntity<GamesList> saveListGames(@RequestBody NewGamesListDto newGamesListDto) {
        return ResponseEntity.ok(gameService.createGamesList(newGamesListDto.name(), newGamesListDto.rawgGames()));
    }

    @GetMapping(value = "/getGamesLists")
    public ResponseEntity<List<GamesList>> getListGames() {
        return ResponseEntity.ok(gameService.getListGames());
    }

    @DeleteMapping(value = "/updateGamesList")
    public void updateGamesList(@RequestBody UpdateGamesListDto updateGamesListDto){
        gameService.updateGamesList(updateGamesListDto);
    }
}
