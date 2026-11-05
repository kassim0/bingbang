package com.stars.bigbang.igdb;

import com.stars.bigbang.dto.igdbDto.TwitchTokenDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.Map;

@FeignClient(name = "TwitchAuthAPI", url = "${igdb.auth-url}")
public interface TwitchAuthApi {

    /**
     * Le secret passe dans le corps et non dans l'URL : Feign recopie l'URL dans ses messages d'erreur,
     * il se retrouverait sinon dans les logs.
     */
    @PostMapping(value = "/oauth2/token", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    TwitchTokenDto getToken(Map<String, ?> form);
}
