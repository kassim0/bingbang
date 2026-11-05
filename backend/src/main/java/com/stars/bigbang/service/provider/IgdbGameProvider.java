package com.stars.bigbang.service.provider;

import com.stars.bigbang.dto.igdbDto.IgdbGameDto;
import com.stars.bigbang.dto.response.GameSearchResult;
import com.stars.bigbang.entity.GameSource;
import com.stars.bigbang.igdb.IgdbApi;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;

@Component
@RequiredArgsConstructor
public class IgdbGameProvider implements GameProvider {

    private static final String IMAGE_URL = "https://images.igdb.com/igdb/image/upload/t_cover_big/%s.jpg";

    /** Jeu principal, extension autonome, remake, remaster, version enrichie, portage : ni DLC, ni pack, ni mod. */
    private static final String SEARCH_QUERY = """
            search "%s";
            fields name,slug,first_release_date,cover.image_id;
            where game_type = (0,4,8,9,10,11);
            limit 20;
            """;

    private final IgdbApi igdbApi;
    private final IgdbTokenService tokenService;

    @Override
    public GameSource source() {
        return GameSource.IGDB;
    }

    @Override
    public List<GameSearchResult> search(String name) {
        return callIgdb(SEARCH_QUERY.formatted(sanitize(name))).stream()
                .map(this::toSearchResult)
                .toList();
    }

    private List<IgdbGameDto> callIgdb(String query) {
        try {
            return igdbApi.searchGames(tokenService.getClientId(), bearer(), query);
        } catch (FeignException.Unauthorized e) {
            tokenService.invalidate();
            return igdbApi.searchGames(tokenService.getClientId(), bearer(), query);
        }
    }

    private String bearer() {
        return "Bearer " + tokenService.getToken();
    }

    /** Retire ce qui permettrait à la saisie de sortir de la chaîne et de modifier la requête Apicalypse. */
    private String sanitize(String name) {
        return name.replaceAll("[\"\\\\\\p{Cntrl}]", " ").trim();
    }

    private GameSearchResult toSearchResult(IgdbGameDto game) {
        return new GameSearchResult(
                GameSource.IGDB,
                game.id(),
                game.slug(),
                game.name(),
                toDate(game.first_release_date()),
                game.cover() == null || game.cover().image_id() == null ? null : IMAGE_URL.formatted(game.cover().image_id())
        );
    }

    private LocalDate toDate(Long timestamp) {
        return timestamp == null ? null : LocalDate.ofInstant(Instant.ofEpochSecond(timestamp), ZoneOffset.UTC);
    }
}
