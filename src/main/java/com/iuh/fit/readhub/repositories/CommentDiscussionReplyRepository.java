package com.iuh.fit.readhub.repositories;

import com.iuh.fit.readhub.models.Comment;
import com.iuh.fit.readhub.models.CommentDiscussionReply;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface CommentDiscussionReplyRepository  extends JpaRepository<CommentDiscussionReply, Long> {
    List<CommentDiscussionReply> findByParentCommentOrderByCreatedAtDesc(Comment comment);

    @Modifying
    @Query("DELETE FROM CommentDiscussionReply r WHERE r.parentComment = ?1")
    void deleteByParentComment(Comment comment);
}