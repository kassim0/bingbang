package com.stars.bigbang.controller;

import com.stars.bigbang.dto.RawgDto.RawgResultsDto;
import com.stars.bigbang.entity.Game;
import com.stars.bigbang.entity.GameList;
import com.stars.bigbang.service.GameListService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/lists")
@RequiredArgsConstructor
public class GameListController {

    private final GameListService gameListService;

    @PostMapping
    public ResponseEntity<GameList> createList(@RequestBody Map<String, String> body) {
        String name = body.get("name");
        return ResponseEntity.ok(gameListService.createList(name));
    }

    @GetMapping
    public ResponseEntity<List<GameList>> getAllLists() {
        return ResponseEntity.ok(gameListService.findAll());
    }

    @PostMapping("/{listId}/games")
    public ResponseEntity<GameList> addGameToList(@PathVariable Long listId, @RequestBody RawgResultsDto gameDto) {
        Game game = new Game();
        game.setName(gameDto.getName());
        game.setSlug(gameDto.getSlug());
        game.setReleased(gameDto.getReleased());
        game.setBackgroundImage(gameDto.getBackground_image());
        game.setRawgId(gameDto.getId());
        return ResponseEntity.ok(gameListService.addGameToList(listId, game));
    }

}
