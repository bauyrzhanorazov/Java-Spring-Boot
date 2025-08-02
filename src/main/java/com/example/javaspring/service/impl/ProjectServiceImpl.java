package com.example.javaspring.service.impl;

import com.example.javaspring.dto.request.ProjectCreateRequest;
import com.example.javaspring.dto.request.ProjectUpdateRequest;
import com.example.javaspring.dto.response.PageResponse;
import com.example.javaspring.dto.response.ProjectResponse;
import com.example.javaspring.dto.response.ProjectSummaryResponse;
import com.example.javaspring.entity.Project;
import com.example.javaspring.entity.User;
import com.example.javaspring.enums.ProjectStatus;
import com.example.javaspring.exception.ResourceNotFoundException;
import com.example.javaspring.mapper.ProjectMapper;
import com.example.javaspring.repository.ProjectRepository;
import com.example.javaspring.repository.UserRepository;
import com.example.javaspring.service.ProjectService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProjectServiceImpl implements ProjectService {

    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;
    private final ProjectMapper projectMapper;

    @Override
    @Transactional
    public ProjectResponse createProject(ProjectCreateRequest request, UUID ownerId) {
        log.debug("Creating project: {} for owner: {}", request.getName(), ownerId);

        User owner = findUserById(ownerId);

        Project project = Project.builder()
                .name(request.getName())
                .description(request.getDescription())
                .status(request.getStatus() != null ? request.getStatus() : ProjectStatus.ACTIVE)
                .deadline(request.getDeadline())
                .owner(owner)
                .members(new HashSet<>())
                .build();

        // Add members if provided
        if (request.getMemberIds() != null && !request.getMemberIds().isEmpty()) {
            Set<User> members = request.getMemberIds().stream()
                    .map(this::findUserById)
                    .collect(Collectors.toSet());
            project.setMembers(members);
        }

        Project savedProject = projectRepository.save(project);
        log.info("Project created successfully with ID: {}", savedProject.getId());

        return projectMapper.toResponse(savedProject);
    }

    @Override
    public ProjectResponse getProjectById(UUID id) {
        log.debug("Fetching project by ID: {}", id);

        Project project = findProjectById(id);
        return projectMapper.toResponse(project);
    }

    @Override
    public PageResponse<ProjectResponse> getAllProjects(Pageable pageable) {
        log.debug("Fetching all projects with pagination: {}", pageable);

        Page<Project> projectPage = projectRepository.findAll(pageable);
        List<ProjectResponse> projectResponses = projectMapper.toResponseList(projectPage.getContent());

        return PageResponse.<ProjectResponse>builder()
                .content(projectResponses)
                .page(projectPage.getNumber())
                .size(projectPage.getSize())
                .totalElements(projectPage.getTotalElements())
                .totalPages(projectPage.getTotalPages())
                .first(projectPage.isFirst())
                .last(projectPage.isLast())
                .hasNext(projectPage.hasNext())
                .hasPrevious(projectPage.hasPrevious())
                .build();
    }

    @Override
    @Transactional
    public ProjectResponse updateProject(UUID id, ProjectUpdateRequest request) {
        log.debug("Updating project with ID: {}", id);

        Project project = findProjectById(id);

        if (request.getName() != null) {
            project.setName(request.getName());
        }

        if (request.getDescription() != null) {
            project.setDescription(request.getDescription());
        }

        if (request.getStatus() != null) {
            project.setStatus(request.getStatus());
        }

        if (request.getDeadline() != null) {
            project.setDeadline(request.getDeadline());
        }

        if (request.getMemberIds() != null) {
            Set<User> members = request.getMemberIds().stream()
                    .map(this::findUserById)
                    .collect(Collectors.toSet());
            project.setMembers(members);
        }

        Project updatedProject = projectRepository.save(project);
        log.info("Project updated successfully with ID: {}", updatedProject.getId());

        return projectMapper.toResponse(updatedProject);
    }

    @Override
    @Transactional
    public void deleteProject(UUID id) {
        log.debug("Deleting project with ID: {}", id);

        Project project = findProjectById(id);
        projectRepository.delete(project);

        log.info("Project deleted successfully with ID: {}", id);
    }

    @Override
    public List<ProjectResponse> getProjectsByOwner(UUID ownerId) {
        log.debug("Fetching projects by owner ID: {}", ownerId);

        List<Project> projects = projectRepository.findByOwnerId(ownerId);
        return projectMapper.toResponseList(projects);
    }

    @Override
    public List<ProjectResponse> getProjectsByMember(UUID memberId) {
        log.debug("Fetching projects by member ID: {}", memberId);

        List<Project> projects = projectRepository.findByMemberId(memberId);
        return projectMapper.toResponseList(projects);
    }

    @Override
    public List<ProjectResponse> getProjectsByUserInvolved(UUID userId) {
        log.debug("Fetching projects by user involved ID: {}", userId);

        List<Project> projects = projectRepository.findByUserInvolved(userId);
        return projectMapper.toResponseList(projects);
    }

    @Override
    @Transactional
    public ProjectResponse addMember(UUID projectId, UUID userId) {
        log.debug("Adding member {} to project {}", userId, projectId);

        Project project = findProjectById(projectId);
        User user = findUserById(userId);

        project.getMembers().add(user);
        Project updatedProject = projectRepository.save(project);

        log.info("Member {} added to project {}", userId, projectId);
        return projectMapper.toResponse(updatedProject);
    }

    @Override
    @Transactional
    public ProjectResponse removeMember(UUID projectId, UUID userId) {
        log.debug("Removing member {} from project {}", userId, projectId);

        Project project = findProjectById(projectId);
        User user = findUserById(userId);

        project.getMembers().remove(user);
        Project updatedProject = projectRepository.save(project);

        log.info("Member {} removed from project {}", userId, projectId);
        return projectMapper.toResponse(updatedProject);
    }

    @Override
    @Transactional
    public ProjectResponse addMembers(UUID projectId, Set<UUID> userIds) {
        log.debug("Adding {} members to project {}", userIds.size(), projectId);

        Project project = findProjectById(projectId);
        Set<User> users = userIds.stream()
                .map(this::findUserById)
                .collect(Collectors.toSet());

        project.getMembers().addAll(users);
        Project updatedProject = projectRepository.save(project);

        log.info("{} members added to project {}", userIds.size(), projectId);
        return projectMapper.toResponse(updatedProject);
    }

    @Override
    @Transactional
    public ProjectResponse removeMembers(UUID projectId, Set<UUID> userIds) {
        log.debug("Removing {} members from project {}", userIds.size(), projectId);

        Project project = findProjectById(projectId);
        Set<User> users = userIds.stream()
                .map(this::findUserById)
                .collect(Collectors.toSet());

        project.getMembers().removeAll(users);
        Project updatedProject = projectRepository.save(project);

        log.info("{} members removed from project {}", userIds.size(), projectId);
        return projectMapper.toResponse(updatedProject);
    }

    @Override
    public List<ProjectResponse> getProjectsByStatus(ProjectStatus status) {
        log.debug("Fetching projects by status: {}", status);

        List<Project> projects = projectRepository.findByStatus(status);
        return projectMapper.toResponseList(projects);
    }

    @Override
    @Transactional
    public ProjectResponse updateProjectStatus(UUID id, ProjectStatus status) {
        log.debug("Updating project {} status to {}", id, status);

        Project project = findProjectById(id);
        project.setStatus(status);

        Project updatedProject = projectRepository.save(project);
        log.info("Project {} status updated to {}", id, status);

        return projectMapper.toResponse(updatedProject);
    }

    @Override
    public List<ProjectResponse> getOverdueProjects() {
        log.debug("Fetching overdue projects");

        List<Project> projects = projectRepository.findOverdueProjects(LocalDateTime.now());
        return projectMapper.toResponseList(projects);
    }

    @Override
    public List<ProjectResponse> getProjectsDueSoon(LocalDateTime start, LocalDateTime end) {
        log.debug("Fetching projects due between {} and {}", start, end);

        List<Project> projects = projectRepository.findProjectsDueSoon(start, end);
        return projectMapper.toResponseList(projects);
    }

    @Override
    public List<ProjectResponse> getProjectsWithDeadlineBefore(LocalDateTime deadline) {
        log.debug("Fetching projects with deadline before: {}", deadline);

        List<Project> projects = projectRepository.findByDeadlineBefore(deadline);
        return projectMapper.toResponseList(projects);
    }

    @Override
    public List<ProjectResponse> getProjectsWithDeadlineBetween(LocalDateTime start, LocalDateTime end) {
        log.debug("Fetching projects with deadline between {} and {}", start, end);

        List<Project> projects = projectRepository.findByDeadlineBetween(start, end);
        return projectMapper.toResponseList(projects);
    }

    @Override
    public List<ProjectResponse> searchProjects(String search) {
        log.debug("Searching projects with query: {}", search);

        List<Project> projects = projectRepository.searchProjects(search);
        return projectMapper.toResponseList(projects);
    }

    @Override
    public List<ProjectSummaryResponse> getRecentProjectsByOwner(UUID ownerId) {
        log.debug("Fetching recent projects by owner: {}", ownerId);

        List<Project> projects = projectRepository.findRecentProjectsByOwner(ownerId);
        return projectMapper.toSummaryResponseList(projects);
    }

    @Override
    public List<ProjectSummaryResponse> getActiveProjectsByMember(UUID memberId) {
        log.debug("Fetching active projects by member: {}", memberId);

        List<Project> projects = projectRepository.findActiveProjectsByMember(memberId);
        return projectMapper.toSummaryResponseList(projects);
    }

    @Override
    public long getTotalProjectsCount() {
        return projectRepository.count();
    }

    @Override
    public long getProjectsByStatusCount(ProjectStatus status) {
        return projectRepository.countByStatus(status);
    }

    @Override
    public long getProjectMembersCount(UUID projectId) {
        return projectRepository.countMembersByProjectId(projectId);
    }

    @Override
    public List<Object[]> getProjectStatusStatistics() {
        return projectRepository.getProjectStatusStatistics();
    }

    @Override
    public List<ProjectResponse> getProjectsWithMinMembers(int memberCount) {
        log.debug("Fetching projects with min {} members", memberCount);

        List<Project> projects = projectRepository.findProjectsWithMinMembers(memberCount);
        return projectMapper.toResponseList(projects);
    }

    @Override
    public List<ProjectResponse> getProjectsWithMoreTasks(int taskCount) {
        log.debug("Fetching projects with more than {} tasks", taskCount);

        List<Project> projects = projectRepository.findProjectsWithMoreTasks(taskCount);
        return projectMapper.toResponseList(projects);
    }

    private Project findProjectById(UUID id) {
        return projectRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found with ID: " + id));
    }

    private User findUserById(UUID id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + id));
    }
}