package com.example.javaspring.service.impl;

import com.example.javaspring.dto.request.CommentCreateRequest;
import com.example.javaspring.dto.request.CommentUpdateRequest;
import com.example.javaspring.dto.response.CommentResponse;
import com.example.javaspring.dto.response.PageResponse;
import com.example.javaspring.entity.Comment;
import com.example.javaspring.entity.Task;
import com.example.javaspring.entity.User;
import com.example.javaspring.exception.AccessDeniedException;
import com.example.javaspring.exception.BusinessLogicException;
import com.example.javaspring.exception.ResourceNotFoundException;
import com.example.javaspring.mapper.CommentMapper;
import com.example.javaspring.repository.CommentRepository;
import com.example.javaspring.repository.TaskRepository;
import com.example.javaspring.repository.UserRepository;
import com.example.javaspring.service.CommentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CommentServiceImpl implements CommentService {

    private final CommentRepository commentRepository;
    private final TaskRepository taskRepository;
    private final UserRepository userRepository;
    private final CommentMapper commentMapper;

    // ========== CRUD OPERATIONS ==========

    @Override
    @Transactional
    public CommentResponse createComment(UUID taskId, CommentCreateRequest request, UUID authorId) {
        log.debug("Creating comment for task {} by author {}", taskId, authorId);

        Task task = findTaskById(taskId);
        User author = findUserById(authorId);

        // Validate author can access the task
        validateUserCanAccessTask(author, task);

        Comment comment = Comment.builder()
                .content(request.getContent())
                .task(task)
                .author(author)
                .build();

        Comment savedComment = commentRepository.save(comment);
        log.info("Comment created successfully with ID: {}", savedComment.getId());

        return commentMapper.toResponse(savedComment);
    }

    @Override
    public CommentResponse getCommentById(UUID id) {
        log.debug("Fetching comment by ID: {}", id);

        Comment comment = findCommentById(id);
        return commentMapper.toResponse(comment);
    }

    @Override
    public PageResponse<CommentResponse> getAllComments(Pageable pageable) {
        log.debug("Fetching all comments with pagination: {}", pageable);

        Page<Comment> commentPage = commentRepository.findAll(pageable);
        List<CommentResponse> commentResponses = commentMapper.toResponseList(commentPage.getContent());

        return buildPageResponse(commentPage, commentResponses);
    }

    @Override
    @Transactional
    public CommentResponse updateComment(UUID id, CommentUpdateRequest request, UUID authorId) {
        log.debug("Updating comment {} by author {}", id, authorId);

        Comment comment = findCommentById(id);

        // Validate author can update this comment
        if (!comment.getAuthor().getId().equals(authorId)) {
            throw new AccessDeniedException("update", "comment - only author can update their own comments");
        }

        comment.setContent(request.getContent());
        Comment updatedComment = commentRepository.save(comment);

        log.info("Comment updated successfully with ID: {}", updatedComment.getId());
        return commentMapper.toResponse(updatedComment);
    }

    @Override
    @Transactional
    public void deleteComment(UUID id, UUID authorId) {
        log.debug("Deleting comment {} by author {}", id, authorId);

        Comment comment = findCommentById(id);

        // Validate author can delete this comment
        if (!comment.getAuthor().getId().equals(authorId)) {
            throw new AccessDeniedException("delete", "comment - only author can delete their own comments");
        }

        commentRepository.delete(comment);
        log.info("Comment deleted successfully with ID: {}", id);
    }

    // ========== TASK RELATED ==========

    @Override
    public List<CommentResponse> getCommentsByTask(UUID taskId) {
        log.debug("Fetching comments by task ID: {}", taskId);

        List<Comment> comments = commentRepository.findByTaskIdOrderByCreatedAtAsc(taskId);
        return commentMapper.toResponseList(comments);
    }

    @Override
    public List<CommentResponse> getCommentsByTaskOrderedByDate(UUID taskId, boolean ascending) {
        log.debug("Fetching comments by task {} ordered by date (ascending: {})", taskId, ascending);

        List<Comment> comments = ascending ?
                commentRepository.findByTaskIdOrderByCreatedAtAsc(taskId) :
                commentRepository.findByTaskIdOrderByCreatedAtDesc(taskId);

        return commentMapper.toResponseList(comments);
    }

    @Override
    public PageResponse<CommentResponse> getCommentsByTask(UUID taskId, Pageable pageable) {
        log.debug("Fetching comments by task {} with pagination: {}", taskId, pageable);

        Page<Comment> commentPage = commentRepository.findByTaskId(taskId, pageable);
        List<CommentResponse> commentResponses = commentMapper.toResponseList(commentPage.getContent());

        return buildPageResponse(commentPage, commentResponses);
    }

    // ========== AUTHOR RELATED ==========

    @Override
    public List<CommentResponse> getCommentsByAuthor(UUID authorId) {
        log.debug("Fetching comments by author ID: {}", authorId);

        List<Comment> comments = commentRepository.findByAuthorId(authorId);
        return commentMapper.toResponseList(comments);
    }

    @Override
    public List<CommentResponse> getRecentCommentsByAuthor(UUID authorId) {
        log.debug("Fetching recent comments by author: {}", authorId);

        List<Comment> comments = commentRepository.findRecentCommentsByAuthor(authorId);
        return commentMapper.toResponseList(comments);
    }

    @Override
    public PageResponse<CommentResponse> getCommentsByAuthor(UUID authorId, Pageable pageable) {
        log.debug("Fetching comments by author {} with pagination: {}", authorId, pageable);

        Page<Comment> commentPage = commentRepository.findByAuthorId(authorId, pageable);
        List<CommentResponse> commentResponses = commentMapper.toResponseList(commentPage.getContent());

        return buildPageResponse(commentPage, commentResponses);
    }

    // ========== PROJECT RELATED ==========

    @Override
    public long getCommentsCountByProject(UUID projectId) {
        return commentRepository.countByProjectId(projectId);
    }

    @Override
    public List<CommentResponse> getRecentCommentsByProject(UUID projectId) {
        log.debug("Fetching recent comments by project: {}", projectId);

        List<Comment> comments = commentRepository.findRecentCommentsByProject(projectId);
        return commentMapper.toResponseList(comments);
    }

    @Override
    public List<CommentResponse> getCommentsByProjectAndAuthor(UUID projectId, UUID authorId) {
        log.debug("Fetching comments by project {} and author {}", projectId, authorId);

        List<Comment> comments = commentRepository.findByProjectAndAuthor(projectId, authorId);
        return commentMapper.toResponseList(comments);
    }

    // ========== SEARCH ==========

    @Override
    public List<CommentResponse> searchComments(String search) {
        log.debug("Searching comments with query: {}", search);

        List<Comment> comments = commentRepository.searchComments(search);
        return commentMapper.toResponseList(comments);
    }

    @Override
    public List<CommentResponse> searchCommentsInTask(UUID taskId, String search) {
        log.debug("Searching comments in task {} with query: {}", taskId, search);

        List<Comment> comments = commentRepository.searchCommentsInTask(taskId, search);
        return commentMapper.toResponseList(comments);
    }

    // ========== TIME BASED ==========

    @Override
    public List<CommentResponse> getCommentsAfter(LocalDateTime date) {
        log.debug("Fetching comments after: {}", date);

        List<Comment> comments = commentRepository.findByCreatedAtAfter(date);
        return commentMapper.toResponseList(comments);
    }

    @Override
    public List<CommentResponse> getCommentsBetween(LocalDateTime start, LocalDateTime end) {
        log.debug("Fetching comments between {} and {}", start, end);

        List<Comment> comments = commentRepository.findByCreatedAtBetween(start, end);
        return commentMapper.toResponseList(comments);
    }

    @Override
    public List<CommentResponse> getRecentCommentsByTask(UUID taskId, LocalDateTime since) {
        log.debug("Fetching recent comments by task {} since {}", taskId, since);

        List<Comment> comments = commentRepository.findRecentCommentsByTask(taskId, since);
        return commentMapper.toResponseList(comments);
    }

    // ========== STATISTICS ==========

    @Override
    public long getTotalCommentsCount() {
        return commentRepository.count();
    }

    @Override
    public long getCommentsByTaskCount(UUID taskId) {
        return commentRepository.countByTaskId(taskId);
    }

    @Override
    public long getCommentsByAuthorCount(UUID authorId) {
        return commentRepository.countByAuthorId(authorId);
    }

    @Override
    public long getCommentsCreatedAfterCount(LocalDateTime date) {
        return commentRepository.countCommentsCreatedAfter(date);
    }

    @Override
    public List<Object[]> getMostActiveCommentAuthors() {
        return commentRepository.getMostActiveCommentAuthors();
    }

    @Override
    public List<Object[]> getMostActiveCommentAuthorsByProject(UUID projectId) {
        return commentRepository.getMostActiveCommentAuthorsByProject(projectId);
    }

    // ========== VALIDATION ==========

    @Override
    public boolean isCommentAuthor(UUID commentId, UUID userId) {
        Comment comment = findCommentById(commentId);
        return comment.getAuthor().getId().equals(userId);
    }

    @Override
    public boolean canUserAccessComment(UUID commentId, UUID userId) {
        Comment comment = findCommentById(commentId);
        User user = findUserById(userId);

        try {
            validateUserCanAccessTask(user, comment.getTask());
            return true;
        } catch (BusinessLogicException | AccessDeniedException e) {
            return false;
        }
    }

    // ========== PRIVATE HELPER METHODS ==========

    private Comment findCommentById(UUID id) {
        return commentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Comment not found with ID: " + id));
    }

    private Task findTaskById(UUID id) {
        return taskRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found with ID: " + id));
    }

    private User findUserById(UUID id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + id));
    }

    private void validateUserCanAccessTask(User user, Task task) {
        boolean canAccess = task.getProject().getOwner().getId().equals(user.getId()) ||
                task.getProject().getMembers().contains(user) ||
                (task.getAssignee() != null && task.getAssignee().getId().equals(user.getId())) ||
                task.getReporter().getId().equals(user.getId());

        if (!canAccess) {
            throw new BusinessLogicException("access task", "user does not have permission to access this task");
        }
    }

    private PageResponse<CommentResponse> buildPageResponse(Page<Comment> commentPage, List<CommentResponse> commentResponses) {
        return PageResponse.<CommentResponse>builder()
                .content(commentResponses)
                .page(commentPage.getNumber())
                .size(commentPage.getSize())
                .totalElements(commentPage.getTotalElements())
                .totalPages(commentPage.getTotalPages())
                .first(commentPage.isFirst())
                .last(commentPage.isLast())
                .hasNext(commentPage.hasNext())
                .hasPrevious(commentPage.hasPrevious())
                .build();
    }
}