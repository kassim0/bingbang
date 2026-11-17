package com.stars.bigbang.service.provider;

import com.stars.bigbang.dto.rawgDto.RawgResponseDto;
import com.stars.bigbang.dto.rawgDto.RawgResultsDto;
import com.stars.bigbang.dto.response.GameSearchResult;
import com.stars.bigbang.entity.GameSource;
import com.stars.bigbang.rawg.RawgApi;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RawgGameProviderTest {

    @Mock
    private RawgApi rawgApi;

    @InjectMocks
    private RawgGameProvider provider;

    private void rawgAnswers(List<RawgResultsDto> results) {
        RawgResponseDto response = new RawgResponseDto();
        response.setResults(results);
        when(rawgApi.searchGamesByName("portal")).thenReturn(response);
    }

    @Test
    void mapsRawgGameToSearchResult() {
        rawgAnswers(List.of(new RawgResultsDto(4200, "portal-2", "Portal 2", "2011-04-18", false, "https://media.rawg.io/portal.jpg")));

        GameSearchResult result = provider.search("portal").get(0);

        assertEquals(new GameSearchResult(GameSource.RAWG, 4200, "portal-2", "Portal 2",
                LocalDate.of(2011, 4, 18), "https://media.rawg.io/portal.jpg"), result);
    }

    @Test
    void gameWithoutReleaseDateIsStillReturned() {
        rawgAnswers(List.of(new RawgResultsDto(1, "tba-game", "Not released yet", null, true, null)));

        assertNull(provider.search("portal").get(0).releaseDate());
    }

    @Test
    void unreadableReleaseDateIsIgnoredInsteadOfFailingTheSearch() {
        rawgAnswers(List.of(new RawgResultsDto(1, "weird", "Weird date", "TBA 2027", false, null)));

        assertNull(provider.search("portal").get(0).releaseDate());
    }

    @Test
    void answerWithoutResultsFieldGivesAnEmptyList() {
        rawgAnswers(null);

        assertTrue(provider.search("portal").isEmpty());
    }
}
