package com.example.javaspring.service;

import com.example.javaspring.dto.request.CommentCreateRequest;
import com.example.javaspring.dto.request.CommentUpdateRequest;
import com.example.javaspring.dto.response.CommentResponse;
import com.example.javaspring.dto.response.PageResponse;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface CommentService {

    CommentResponse createComment(UUID taskId, CommentCreateRequest request, UUID authorId);

    CommentResponse getCommentById(UUID id);

    PageResponse<CommentResponse> getAllComments(Pageable pageable);

    CommentResponse updateComment(UUID id, CommentUpdateRequest request, UUID authorId);

    void deleteComment(UUID id, UUID authorId);

    List<CommentResponse> getCommentsByTask(UUID taskId);

    List<CommentResponse> getCommentsByTaskOrderedByDate(UUID taskId, boolean ascending);

    PageResponse<CommentResponse> getCommentsByTask(UUID taskId, Pageable pageable);

    List<CommentResponse> getCommentsByAuthor(UUID authorId);

    List<CommentResponse> getRecentCommentsByAuthor(UUID authorId);

    PageResponse<CommentResponse> getCommentsByAuthor(UUID authorId, Pageable pageable);

    long getCommentsCountByProject(UUID projectId);

    List<CommentResponse> getRecentCommentsByProject(UUID projectId);

    List<CommentResponse> getCommentsByProjectAndAuthor(UUID projectId, UUID authorId);

    List<CommentResponse> searchComments(String search);

    List<CommentResponse> searchCommentsInTask(UUID taskId, String search);

    List<CommentResponse> getCommentsAfter(LocalDateTime date);

    List<CommentResponse> getCommentsBetween(LocalDateTime start, LocalDateTime end);

    List<CommentResponse> getRecentCommentsByTask(UUID taskId, LocalDateTime since);

    long getTotalCommentsCount();

    long getCommentsByTaskCount(UUID taskId);

    long getCommentsByAuthorCount(UUID authorId);

    long getCommentsCreatedAfterCount(LocalDateTime date);

    List<Object[]> getMostActiveCommentAuthors();

    List<Object[]> getMostActiveCommentAuthorsByProject(UUID projectId);

    boolean isCommentAuthor(UUID commentId, UUID userId);

    boolean canUserAccessComment(UUID commentId, UUID userId);
}