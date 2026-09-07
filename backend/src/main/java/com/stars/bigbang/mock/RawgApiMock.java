package com.stars.bigbang.mock;

import com.stars.bigbang.dto.rawgDto.RawgResponseDto;
import com.stars.bigbang.dto.rawgDto.RawgResultsDto;
import com.stars.bigbang.rest.RawgApi;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Profile("mock")
public class RawgApiMock implements RawgApi {

    @Override
    public RawgResponseDto searchGamesByName(String gameName, String token) {
        RawgResponseDto response = new RawgResponseDto();
        response.setCount(3);

        RawgResultsDto game1 = new RawgResultsDto(
        3498,
        "grand-theft-auto-v",
        "Grand Theft Auto V",
        "2013-09-17",
        false,
        "https://media.rawg.io/media/games/456/456dea5e1c7e3cd07060c14e96612001.jpg");

        RawgResultsDto game2 = new RawgResultsDto(
        3328,
        "the-witcher-3-wild-hunt",
        "The Witcher 3: Wild Hunt",
        "2015-05-18",
        false,
        "https://media.rawg.io/media/games/618/618c2031a07bbff6b4f611f10b6bcdbc.jpg");

        RawgResultsDto game3 = new RawgResultsDto(
        4200,
        "portal-2",
        "Portal 2",
        "2011-04-18",
        false,
        "https://media.rawg.io/media/games/328/3283617cb7d75d67257fc58339188742.jpg");

        response.setResults(List.of(game1, game2, game3));
        return response;
    }
}
