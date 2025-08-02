package com.example.javaspring.mapper;

import com.example.javaspring.dto.response.UserResponse;
import com.example.javaspring.dto.response.UserSummaryResponse;
import com.example.javaspring.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

import java.util.List;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface UserMapper {

    UserResponse toResponse(User user);

    List<UserResponse> toResponseList(List<User> users);

    @Mapping(target = "id", source = "id")
    @Mapping(target = "username", source = "username")
    @Mapping(target = "email", source = "email")
    @Mapping(target = "firstName", source = "firstName")
    @Mapping(target = "lastName", source = "lastName")
    UserSummaryResponse toSummaryResponse(User user);

    List<UserSummaryResponse> toSummaryResponseList(List<User> users);
}