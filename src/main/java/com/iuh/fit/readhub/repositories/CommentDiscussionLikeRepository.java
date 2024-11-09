package com.iuh.fit.readhub.repositories;

import com.iuh.fit.readhub.models.Comment;
import com.iuh.fit.readhub.models.CommentDiscussionLike;
import com.iuh.fit.readhub.models.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CommentDiscussionLikeRepository extends JpaRepository<CommentDiscussionLike, Long> {
    boolean existsByCommentAndUser(Comment comment, User user);
    void deleteByCommentAndUser(Comment comment, User user);
    long countByComment(Comment comment);
}
