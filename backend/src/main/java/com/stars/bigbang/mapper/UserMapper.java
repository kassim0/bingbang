package com.stars.bigbang.mapper;

import com.stars.bigbang.dto.UserDto;
import com.stars.bigbang.entity.User;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UserMapper{

    UserDto toDto(User user);

    User toEntity(UserDto userDto);
}
