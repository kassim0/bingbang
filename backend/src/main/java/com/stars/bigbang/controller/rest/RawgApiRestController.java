package com.stars.bigbang.controller.rest;

import com.stars.bigbang.dto.RawgDto.RawgResponseDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;


@FeignClient(name="RawgAPI", url="https://api.rawg.io/api")
public interface RawgApiRestController {

    @GetMapping("/games")
    RawgResponseDto searchGamesByName(@RequestParam("search") String GameName, @RequestParam("key") String token);
}
