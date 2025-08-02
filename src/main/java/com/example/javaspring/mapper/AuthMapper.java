package com.example.javaspring.mapper;

import com.example.javaspring.dto.response.AuthResponse;
import com.example.javaspring.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING, uses = {UserMapper.class})
public interface AuthMapper {

    default AuthResponse toAuthResponse(String accessToken, String refreshToken, Long expiresIn, User user) {
        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresIn(expiresIn)
                .user(user != null ? toUserSummary(user) : null)
                .build();
    }

    // Helper method to create user summary
    @Mapping(target = "id", source = "id")
    @Mapping(target = "username", source = "username")
    @Mapping(target = "email", source = "email")
    @Mapping(target = "firstName", source = "firstName")
    @Mapping(target = "lastName", source = "lastName")
    @Mapping(target = "roles", source = "roles")
    @Mapping(target = "enabled", source = "enabled")
    AuthResponse.UserSummary toUserSummary(User user);
}