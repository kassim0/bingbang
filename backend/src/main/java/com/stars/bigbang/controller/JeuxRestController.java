package com.stars.bigbang.controller;

import com.stars.bigbang.dto.RawgDto.RawgResultsDto;
import com.stars.bigbang.dto.rawgDto.RawgResultsDto;
import com.stars.bigbang.entity.Game;
import com.stars.bigbang.entity.GameList;
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

    @PostMapping(value = "/saveListGames")
    public ResponseEntity<GameList> saveListGames(@RequestBody RawgResultsDto[] gameDto) {
        return ResponseEntity.ok(gameService.saveRawgListGame(gameDto));
    }

    @GetMapping(value = "/getListGames")
    public ResponseEntity<List<GameList>> getListGames() {
        return ResponseEntity.ok(gameService.getListGames());
    }
}
