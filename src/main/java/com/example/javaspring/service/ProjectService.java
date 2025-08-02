package com.example.javaspring.service;

import com.example.javaspring.dto.request.ProjectCreateRequest;
import com.example.javaspring.dto.request.ProjectUpdateRequest;
import com.example.javaspring.dto.response.PageResponse;
import com.example.javaspring.dto.response.ProjectResponse;
import com.example.javaspring.dto.response.ProjectSummaryResponse;
import com.example.javaspring.enums.ProjectStatus;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public interface ProjectService {

    ProjectResponse createProject(ProjectCreateRequest request, UUID ownerId);

    ProjectResponse getProjectById(UUID id);

    PageResponse<ProjectResponse> getAllProjects(Pageable pageable);

    ProjectResponse updateProject(UUID id, ProjectUpdateRequest request);

    void deleteProject(UUID id);

    List<ProjectResponse> getProjectsByOwner(UUID ownerId);

    List<ProjectResponse> getProjectsByMember(UUID memberId);

    List<ProjectResponse> getProjectsByUserInvolved(UUID userId);

    ProjectResponse addMember(UUID projectId, UUID userId);

    ProjectResponse removeMember(UUID projectId, UUID userId);

    ProjectResponse addMembers(UUID projectId, Set<UUID> userIds);

    ProjectResponse removeMembers(UUID projectId, Set<UUID> userIds);

    List<ProjectResponse> getProjectsByStatus(ProjectStatus status);

    ProjectResponse updateProjectStatus(UUID id, ProjectStatus status);

    List<ProjectResponse> getOverdueProjects();

    List<ProjectResponse> getProjectsDueSoon(LocalDateTime start, LocalDateTime end);

    List<ProjectResponse> getProjectsWithDeadlineBefore(LocalDateTime deadline);

    List<ProjectResponse> getProjectsWithDeadlineBetween(LocalDateTime start, LocalDateTime end);

    List<ProjectResponse> searchProjects(String search);

    List<ProjectSummaryResponse> getRecentProjectsByOwner(UUID ownerId);

    List<ProjectSummaryResponse> getActiveProjectsByMember(UUID memberId);

    long getTotalProjectsCount();

    long getProjectsByStatusCount(ProjectStatus status);

    long getProjectMembersCount(UUID projectId);

    List<Object[]> getProjectStatusStatistics();

    List<ProjectResponse> getProjectsWithMinMembers(int memberCount);

    List<ProjectResponse> getProjectsWithMoreTasks(int taskCount);
}