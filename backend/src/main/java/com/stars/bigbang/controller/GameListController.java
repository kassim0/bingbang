package com.stars.bigbang.controller;

import com.stars.bigbang.dto.RawgDto.RawgResultsDto;
import com.stars.bigbang.entity.Game;
import com.stars.bigbang.entity.GameList;
import com.stars.bigbang.service.GameListService;
import com.stars.bigbang.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/lists")
@RequiredArgsConstructor
public class GameListController {

    private final GameListService gameListService;
    private final UserService userService;

    @PostMapping
    public ResponseEntity<GameList> createList(@RequestBody Map<String, String> body, HttpServletRequest request) {
        String name = body.get("name");
        Long userId = getCurrentUserId();
        String deviceId = userId == null ? request.getHeader("X-Device-Id") : null;
        return ResponseEntity.ok(gameListService.createList(name, userId, deviceId));
    }

    @GetMapping
    public ResponseEntity<List<GameList>> getAllLists(HttpServletRequest request) {
        Long userId = getCurrentUserId();
        String deviceId = userId == null ? request.getHeader("X-Device-Id") : null;
        return ResponseEntity.ok(gameListService.findAll(userId, deviceId));
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

    private Long getCurrentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getPrincipal())) {
            return userService.findByEmail(auth.getName())
                    .map(user -> user.getId())
                    .orElse(null);
        }
        return null;
    }
}
