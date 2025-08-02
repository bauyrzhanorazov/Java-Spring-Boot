package com.example.javaspring.mapper;

import com.example.javaspring.dto.response.AuthResponse;
import com.example.javaspring.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING, uses = {UserMapper.class})
public interface AuthMapper {

    @Mapping(target = "accessToken", source = "accessToken")
    @Mapping(target = "refreshToken", source = "refreshToken")
    @Mapping(target = "tokenType", constant = "Bearer")
    @Mapping(target = "expiresIn", source = "expiresIn")
    @Mapping(target = "user", source = "user")
    AuthResponse toAuthResponse(String accessToken, String refreshToken, Long expiresIn, User user);
}