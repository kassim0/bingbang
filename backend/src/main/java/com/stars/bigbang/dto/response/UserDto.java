package com.stars.bigbang.dto.response;

import com.stars.bigbang.entity.User;

public record UserDto(
        Long id,
        String username,
        String email,
        boolean guest
) {
    public static UserDto from(User user) {
        return new UserDto(user.getId(), user.getUsername(), user.getEmail(), user.isGuest());
    }
}
