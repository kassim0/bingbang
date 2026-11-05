package com.stars.bigbang.mock;

import com.stars.bigbang.dto.igdbDto.TwitchTokenDto;
import com.stars.bigbang.rest.TwitchAuthApi;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@Profile("mock")
public class TwitchAuthApiMock implements TwitchAuthApi {

    @Override
    public TwitchTokenDto getToken(Map<String, ?> form) {
        return new TwitchTokenDto("mock-token", 3600);
    }
}
