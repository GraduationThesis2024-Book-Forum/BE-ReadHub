package com.iuh.fit.readhub.repositories;

import com.iuh.fit.readhub.models.Comment;
import com.iuh.fit.readhub.models.Discussion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long> {
    List<Comment> findByDiscussion_DiscussionIdOrderByCreatedAtDesc(Long discussionId);

    @Modifying
    @Transactional
    void deleteByDiscussion(Discussion discussion);
}