package com.stars.bigbang.controller;

import com.stars.bigbang.dto.response.GameSearchResult;
import com.stars.bigbang.service.GameSearchService;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/games")
@AllArgsConstructor
public class GameSearchController {

    private GameSearchService gameSearchService;

    @GetMapping("/search")
    public List<GameSearchResult> search(@RequestParam String name) {
        return gameSearchService.search(name);
    }
}
