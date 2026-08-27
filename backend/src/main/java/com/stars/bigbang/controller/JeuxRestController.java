package com.stars.bigbang.controller;

import com.stars.bigbang.dto.rawgDto.RawgResultsDto;
import com.stars.bigbang.dto.record.NewGamesList;
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

    @PostMapping(value = "/saveGamesList")
    public ResponseEntity<GamesList> saveListGames(@RequestBody NewGamesList newGamesList) {
        return ResponseEntity.ok(gameService.saveRawgGamesList(newGamesList.name(), newGamesList.rawgGames()));
    }

    @GetMapping(value = "/getGamesLists")
    public ResponseEntity<List<GamesList>> getListGames() {
        return ResponseEntity.ok(gameService.getListGames());
    }

    @DeleteMapping(value = "/deleteGamesFromGamesList")
    public void deleteGamesFromGamesList(){

    }
}
