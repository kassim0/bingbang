package com.stars.bigbang.mock;

import com.stars.bigbang.dto.igdbDto.IgdbCoverDto;
import com.stars.bigbang.dto.igdbDto.IgdbGameDto;
import com.stars.bigbang.igdb.IgdbApi;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
@Profile("mock")
public class IgdbApiMock implements IgdbApi {

    /** Retrouve le texte cherché dans la requête Apicalypse : search "zelda"; */
    private static final Pattern SEARCH_TEXT = Pattern.compile("search \"(.*?)\";");

    @Override
    public List<IgdbGameDto> searchGames(String clientId, String authorization, String query) {
        Matcher matcher = SEARCH_TEXT.matcher(query);
        String text = matcher.find() ? matcher.group(1) : "";
        return MockCatalog.search(text).stream()
                .map(game -> new IgdbGameDto(game.id(), game.slug(), game.name(), game.releasedTimestamp(), new IgdbCoverDto(game.imageId())))
                .toList();
    }
}
