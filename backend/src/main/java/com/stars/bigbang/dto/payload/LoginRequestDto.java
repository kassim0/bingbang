package com.stars.bigbang.dto.payload;

public record LoginRequestDto(
        String email,
        String password
) {
}
