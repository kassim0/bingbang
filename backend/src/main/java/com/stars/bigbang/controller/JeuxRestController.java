package com.stars.bigbang.controller;

import com.stars.bigbang.dto.payload.NewGamesListDto;
import com.stars.bigbang.dto.payload.UpdateGamesListDto;
import com.stars.bigbang.dto.response.GamesListDto;
import com.stars.bigbang.security.UserPrincipal;
import com.stars.bigbang.service.GameService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/game")
@AllArgsConstructor
@Validated
public class JeuxRestController {

    private GameService gameService;

    @PutMapping(value = "/newGamesList")
    public ResponseEntity<GamesListDto> saveGamesList(@RequestBody NewGamesListDto newGamesListDto,
                                                      @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(GamesListDto.from(gameService.createGamesList(newGamesListDto, principal.getId())));
    }

    @GetMapping(value = "/getGamesLists")
    public ResponseEntity<List<GamesListDto>> getGamesList(@AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(gameService.getGamesList(principal.getId()));
    }

    @PostMapping(value = "/updateGamesList")
    public ResponseEntity<String> updateGamesList(@RequestBody UpdateGamesListDto updateGamesListDto,
                                                  @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(gameService.updateGamesList(updateGamesListDto, principal.getId()));
    }

    @DeleteMapping(value = "/deleteGamesList/{gamesListId}")
    public ResponseEntity<String> deleteGamesList(@PathVariable Long gamesListId,
                                                  @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(gameService.deleteGamesList(gamesListId, principal.getId()));
    }
}
