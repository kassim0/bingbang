package com.stars.bigbang.controller;

import com.stars.bigbang.dto.RawgDto.RawgResponseDto;
import com.stars.bigbang.dto.RawgDto.RawgResultsDto;
import com.stars.bigbang.entity.Game;
import com.stars.bigbang.rest.RawgApi;
import com.stars.bigbang.service.GameService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/games")
@RequiredArgsConstructor
public class GamesController {

    private final RawgApi rawgApi;
    private final GameService gameService;

    @Value("${rawg.api.key}")
    private String apiKey;

    /**
     * Recherche des jeux par nom
     * GET /api/games/search?query=zelda
     */
    @GetMapping("/search")
    public ResponseEntity<RawgResponseDto> searchGames(@RequestParam("query") String query) {
        RawgResponseDto response = rawgApi.searchGamesByName(query, apiKey);
        return ResponseEntity.ok(response);
    }

    /**
     * Recherche des jeux par nom (version path variable)
     * GET /api/games/search/zelda
     */
    @GetMapping("/search/{gameName}")
    public ResponseEntity<RawgResponseDto> searchGamesByName(@PathVariable String gameName) {
        RawgResponseDto response = rawgApi.searchGamesByName(gameName, apiKey);
        return ResponseEntity.ok(response);
    }

    /**
     * Sauvegarde un jeu dans la base de données
     * POST /api/games
     */
    @PostMapping
    public ResponseEntity<Game> saveGame(@RequestBody RawgResultsDto gameDto) {
        Game game = new Game();
        game.setName(gameDto.getName());
        game.setSlug(gameDto.getSlug());
        game.setReleased(gameDto.getReleased());
        game.setBackgroundImage(gameDto.getBackground_image());
        game.setRawgId(gameDto.getId());
        return ResponseEntity.ok(gameService.save(game));
    }

}
