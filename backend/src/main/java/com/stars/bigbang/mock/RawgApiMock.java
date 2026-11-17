package com.stars.bigbang.mock;

import com.stars.bigbang.dto.rawgDto.RawgResponseDto;
import com.stars.bigbang.dto.rawgDto.RawgResultsDto;
import com.stars.bigbang.rawg.RawgApi;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Profile("mock")
public class RawgApiMock implements RawgApi {

    private static final String IMAGE_URL = "https://images.igdb.com/igdb/image/upload/t_cover_big/%s.jpg";

    @Override
    public RawgResponseDto searchGamesByName(String gameName) {
        List<RawgResultsDto> results = MockCatalog.search(gameName).stream()
                .map(game -> new RawgResultsDto(game.id(), game.slug(), game.name(), game.released(), false, IMAGE_URL.formatted(game.imageId())))
                .toList();

        RawgResponseDto response = new RawgResponseDto();
        response.setCount(results.size());
        response.setResults(results);
        return response;
    }
}
