package com.example.javaspring.repository;

import com.example.javaspring.entity.Comment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface CommentRepository extends BaseRepository<Comment> {

    List<Comment> findByTaskId(UUID taskId);

    List<Comment> findByAuthorId(UUID authorId);

    Page<Comment> findByTaskId(UUID taskId, Pageable pageable);

    Page<Comment> findByAuthorId(UUID authorId, Pageable pageable);

    List<Comment> findByTaskIdOrderByCreatedAtAsc(UUID taskId);

    List<Comment> findByTaskIdOrderByCreatedAtDesc(UUID taskId);

    @Query("SELECT c FROM Comment c WHERE c.task.id = :taskId AND " +
            "LOWER(c.content) LIKE LOWER(CONCAT('%', :search, '%'))")
    List<Comment> searchCommentsInTask(@Param("taskId") UUID taskId, @Param("search") String search);

    @Query("SELECT c FROM Comment c WHERE " +
            "LOWER(c.content) LIKE LOWER(CONCAT('%', :search, '%'))")
    List<Comment> searchComments(@Param("search") String search);

    List<Comment> findByCreatedAtAfter(LocalDateTime date);

    List<Comment> findByCreatedAtBetween(LocalDateTime start, LocalDateTime end);

    @Query("SELECT c FROM Comment c WHERE c.task.id = :taskId AND c.createdAt >= :date")
    List<Comment> findRecentCommentsByTask(@Param("taskId") UUID taskId, @Param("date") LocalDateTime date);

    @Query("SELECT COUNT(c) FROM Comment c WHERE c.task.id = :taskId")
    long countByTaskId(@Param("taskId") UUID taskId);

    @Query("SELECT COUNT(c) FROM Comment c WHERE c.author.id = :authorId")
    long countByAuthorId(@Param("authorId") UUID authorId);

    @Query("SELECT COUNT(c) FROM Comment c WHERE c.task.project.id = :projectId")
    long countByProjectId(@Param("projectId") UUID projectId);

    @Query("SELECT COUNT(c) FROM Comment c WHERE c.createdAt >= :date")
    long countCommentsCreatedAfter(@Param("date") LocalDateTime date);

    @Query("SELECT c FROM Comment c WHERE c.task.project.id = :projectId ORDER BY c.createdAt DESC")
    List<Comment> findRecentCommentsByProject(@Param("projectId") UUID projectId);

    @Query("SELECT c FROM Comment c WHERE c.task.project.id = :projectId AND c.author.id = :authorId")
    List<Comment> findByProjectAndAuthor(@Param("projectId") UUID projectId, @Param("authorId") UUID authorId);

    @Query("SELECT c FROM Comment c WHERE c.author.id = :authorId ORDER BY c.createdAt DESC")
    List<Comment> findRecentCommentsByAuthor(@Param("authorId") UUID authorId);

    @Query("SELECT c.author.id, COUNT(c) FROM Comment c GROUP BY c.author.id ORDER BY COUNT(c) DESC")
    List<Object[]> getMostActiveCommentAuthors();

    @Query("SELECT c.author.id, COUNT(c) FROM Comment c WHERE c.task.project.id = :projectId GROUP BY c.author.id ORDER BY COUNT(c) DESC")
    List<Object[]> getMostActiveCommentAuthorsByProject(@Param("projectId") UUID projectId);
}