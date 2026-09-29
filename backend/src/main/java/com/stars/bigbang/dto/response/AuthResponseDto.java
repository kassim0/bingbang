package com.stars.bigbang.dto.response;

public record AuthResponseDto(
        String token,
        UserDto user
) {
}
