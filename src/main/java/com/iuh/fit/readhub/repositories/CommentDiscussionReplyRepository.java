package com.iuh.fit.readhub.repositories;

import com.iuh.fit.readhub.models.Comment;
import com.iuh.fit.readhub.models.CommentDiscussionReply;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CommentDiscussionReplyRepository  extends JpaRepository<CommentDiscussionReply, Long> {
    List<CommentDiscussionReply> findByParentCommentOrderByCreatedAtDesc(Comment comment);
}