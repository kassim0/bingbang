package com.stars.bigbang.mock;

import com.stars.bigbang.dto.igdbDto.IgdbCoverDto;
import com.stars.bigbang.dto.igdbDto.IgdbGameDto;
import com.stars.bigbang.igdb.IgdbApi;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Profile("mock")
public class IgdbApiMock implements IgdbApi {

    @Override
    public List<IgdbGameDto> searchGames(String clientId, String authorization, String query) {
        return List.of(
                new IgdbGameDto(1020, "grand-theft-auto-v", "Grand Theft Auto V", 1379376000L, new IgdbCoverDto("co2lbd")),
                new IgdbGameDto(1942, "the-witcher-3-wild-hunt", "The Witcher 3: Wild Hunt", 1431993600L, new IgdbCoverDto("coaarl")),
                new IgdbGameDto(72, "portal-2", "Portal 2", 1303171200L, new IgdbCoverDto("co1rs4"))
        );
    }
}
