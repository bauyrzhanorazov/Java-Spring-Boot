package com.example.javaspring.mapper;

import com.example.javaspring.dto.response.CommentResponse;
import com.example.javaspring.entity.Comment;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

import java.util.List;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING, uses = {UserMapper.class})
public interface CommentMapper {

    @Mapping(target = "author", source = "author")
    CommentResponse toResponse(Comment comment);

    List<CommentResponse> toResponseList(List<Comment> comments);
}