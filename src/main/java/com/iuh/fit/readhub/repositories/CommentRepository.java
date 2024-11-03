package com.iuh.fit.readhub.repositories;

import com.iuh.fit.readhub.models.Comment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long> {
    List<Comment> findByDiscussion_DiscussionIdOrderByCreatedAtDesc(Long discussionId);
}