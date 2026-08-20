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

        RawgResultsDto game1 = new RawgResultsDto();
        game1.setId(3498);
        game1.setSlug("grand-theft-auto-v");
        game1.setName("Grand Theft Auto V");
        game1.setReleased("2013-09-17");
        game1.setTba(false);
        game1.setBackground_image("https://media.rawg.io/media/games/456/456dea5e1c7e3cd07060c14e96612001.jpg");

        RawgResultsDto game2 = new RawgResultsDto();
        game2.setId(3328);
        game2.setSlug("the-witcher-3-wild-hunt");
        game2.setName("The Witcher 3: Wild Hunt");
        game2.setReleased("2015-05-18");
        game2.setTba(false);
        game2.setBackground_image("https://media.rawg.io/media/games/618/618c2031a07bbff6b4f611f10b6bcdbc.jpg");

        RawgResultsDto game3 = new RawgResultsDto();
        game3.setId(4200);
        game3.setSlug("portal-2");
        game3.setName("Portal 2");
        game3.setReleased("2011-04-18");
        game3.setTba(false);
        game3.setBackground_image("https://media.rawg.io/media/games/328/3283617cb7d75d67257fc58339188742.jpg");

        response.setResults(List.of(game1, game2, game3));
        return response;
    }
}
