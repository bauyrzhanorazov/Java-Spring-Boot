package com.example.javaspring.mapper;

import com.example.javaspring.dto.response.TaskResponse;
import com.example.javaspring.dto.response.TaskSummaryResponse;
import com.example.javaspring.entity.Task;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

import java.util.List;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING,
        uses = {UserMapper.class, ProjectMapper.class, CommentMapper.class})
public interface TaskMapper {

    @Mapping(target = "project", source = "project")
    @Mapping(target = "assignee", source = "assignee")
    @Mapping(target = "reporter", source = "reporter")
    @Mapping(target = "comments", source = "comments")
    TaskResponse toResponse(Task task);

    List<TaskResponse> toResponseList(List<Task> tasks);

    @Mapping(target = "id", source = "id")
    @Mapping(target = "title", source = "title")
    @Mapping(target = "status", source = "status")
    @Mapping(target = "priority", source = "priority")
    @Mapping(target = "dueDate", source = "dueDate")
    @Mapping(target = "assignee", source = "assignee")
    @Mapping(target = "commentsCount", expression = "java(task.getComments() != null ? task.getComments().size() : 0)")
    TaskSummaryResponse toSummaryResponse(Task task);

    List<TaskSummaryResponse> toSummaryResponseList(List<Task> tasks);
}