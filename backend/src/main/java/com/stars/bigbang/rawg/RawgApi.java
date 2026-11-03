package com.stars.bigbang.rawg;

import com.stars.bigbang.dto.rawgDto.RawgResponseDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;


@FeignClient(name="RawgAPI", url="${rawg.url}",configuration = RawgFeignConfig.class)
public interface RawgApi {

    @GetMapping("/games")
    RawgResponseDto searchGamesByName(@RequestParam("search") String GameName);
}
