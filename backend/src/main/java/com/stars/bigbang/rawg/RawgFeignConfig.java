package com.stars.bigbang.rawg;

import feign.RequestInterceptor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;

public class RawgFeignConfig {

    @Value("${bingbang.rawgApiSecret}")
    private String rawgApiKey;

    @Bean
    public RequestInterceptor authRequestInterceptor() {
        return template -> template.query("key", rawgApiKey);
    }
}
