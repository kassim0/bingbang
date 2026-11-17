package com.stars.bigbang.controller;

import com.stars.bigbang.dto.response.GameSearchResult;
import com.stars.bigbang.service.GameSearchService;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/games")
@RequiredArgsConstructor
public class GameSearchController {

    private final GameSearchService gameSearchService;

    @GetMapping("/search")
    public List<GameSearchResult> search(@RequestParam @Size(max = 100) String name) {
        return gameSearchService.search(name);
    }
}
