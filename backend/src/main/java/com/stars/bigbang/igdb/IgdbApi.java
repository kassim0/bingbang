package com.stars.bigbang.igdb;

import com.stars.bigbang.dto.igdbDto.IgdbGameDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

import java.util.List;

@FeignClient(name = "IgdbAPI", url = "${igdb.url}")
public interface IgdbApi {

    /**
     * @param query requête au format Apicalypse, le langage de requête d'IGDB
     */
    @PostMapping(value = "/games", consumes = "text/plain;charset=UTF-8")
    List<IgdbGameDto> searchGames(@RequestHeader("Client-ID") String clientId,
                                  @RequestHeader("Authorization") String authorization,
                                  @RequestBody String query);
}
