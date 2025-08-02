package com.example.javaspring.mapper;

import com.example.javaspring.dto.response.ProjectResponse;
import com.example.javaspring.dto.response.ProjectSummaryResponse;
import com.example.javaspring.entity.Project;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

import java.util.List;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING, uses = {UserMapper.class})
public interface ProjectMapper {

    @Mapping(target = "owner", source = "owner")
    @Mapping(target = "members", source = "members")
    @Mapping(target = "tasksCount", expression = "java(project.getTasks() != null ? project.getTasks().size() : 0)")
    ProjectResponse toResponse(Project project);

    List<ProjectResponse> toResponseList(List<Project> projects);

    @Mapping(target = "id", source = "id")
    @Mapping(target = "name", source = "name")
    @Mapping(target = "status", source = "status")
    @Mapping(target = "deadline", source = "deadline")
    @Mapping(target = "owner", source = "owner")
    @Mapping(target = "tasksCount", expression = "java(project.getTasks() != null ? project.getTasks().size() : 0)")
    ProjectSummaryResponse toSummaryResponse(Project project);

    List<ProjectSummaryResponse> toSummaryResponseList(List<Project> projects);
}