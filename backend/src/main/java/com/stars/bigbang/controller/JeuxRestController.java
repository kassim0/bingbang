package com.stars.bigbang.controller;

import com.stars.bigbang.dto.record.NewGamesListDto;
import com.stars.bigbang.dto.record.UpdateGamesListDto;
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
@RequestMapping("/api")
@AllArgsConstructor
@Validated
public class JeuxRestController {

    private GameService gameService;

    @PutMapping(value = "/newGamesList")
    public ResponseEntity<GamesListDto> saveListGames(@RequestBody NewGamesListDto newGamesListDto,
                                                        @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(GamesListDto.from(gameService.createGamesList(newGamesListDto.name(), newGamesListDto.rawgGames(), principal.getUser())));
    }

    @GetMapping(value = "/getGamesLists")
    public ResponseEntity<List<GamesListDto>> getListGames(@AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(gameService.getListGames(principal.getUser()));
    }

    @PostMapping(value = "/updateGamesList")
    public ResponseEntity<String> updateGamesList(@RequestBody UpdateGamesListDto updateGamesListDto,
                                                   @AuthenticationPrincipal UserPrincipal principal){
       return ResponseEntity.ok(gameService.updateGamesList(updateGamesListDto, principal.getUser()));
    }

    @DeleteMapping(value = "/deleteGamesList/{GamesListId}")
    public ResponseEntity<String> deleteGamesList(@PathVariable Long GamesListId,
                                                   @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(gameService.deleteGamesList(GamesListId, principal.getUser()));
    }
}
