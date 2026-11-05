package com.stars.bigbang.config;

import com.stars.bigbang.igdb.IgdbApi;
import com.stars.bigbang.igdb.TwitchAuthApi;
import com.stars.bigbang.rawg.RawgApi;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile("!mock")
@EnableFeignClients(clients = {RawgApi.class, IgdbApi.class, TwitchAuthApi.class})
public class FeignConfig {
}
