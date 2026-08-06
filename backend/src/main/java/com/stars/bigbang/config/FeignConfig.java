package com.stars.bigbang.config;

import com.stars.bigbang.rest.RawgApi;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile("!mock")
@EnableFeignClients(clients = RawgApi.class)
public class FeignConfig {
}
