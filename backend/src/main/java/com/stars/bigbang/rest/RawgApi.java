package com.stars.bigbang.rest;

import com.stars.bigbang.dto.RawgResponseDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;


@FeignClient(name="RawgAPI", url="https://api.rawg.io/api")
public interface RawgApi {

    @GetMapping("/games")
    RawgResponseDto searchGamesByName(@RequestParam("search") String GameName, @RequestParam("key") String token);
}
