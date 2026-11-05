package com.stars.bigbang.service.provider;

import com.stars.bigbang.dto.rawgDto.RawgResponseDto;
import com.stars.bigbang.dto.rawgDto.RawgResultsDto;
import com.stars.bigbang.dto.response.GameSearchResult;
import com.stars.bigbang.entity.GameSource;
import com.stars.bigbang.rawg.RawgApi;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;

@Component
@RequiredArgsConstructor
public class RawgGameProvider implements GameProvider {

    private final RawgApi rawgApi;

    @Override
    public GameSource source() {
        return GameSource.RAWG;
    }

    @Override
    public List<GameSearchResult> search(String name) {
        RawgResponseDto response = rawgApi.searchGamesByName(name);
        if (response.getResults() == null) {
            return List.of();
        }
        return response.getResults().stream().map(this::toSearchResult).toList();
    }

    private GameSearchResult toSearchResult(RawgResultsDto game) {
        return new GameSearchResult(
                GameSource.RAWG,
                game.id(),
                game.slug(),
                game.name(),
                parseDate(game.released()),
                game.background_image()
        );
    }

    private LocalDate parseDate(String date) {
        if (date == null) {
            return null;
        }
        try {
            return LocalDate.parse(date);
        } catch (DateTimeParseException e) {
            return null;
        }
    }
}
