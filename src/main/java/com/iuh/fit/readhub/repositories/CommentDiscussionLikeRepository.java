package com.iuh.fit.readhub.repositories;

import com.iuh.fit.readhub.models.Comment;
import com.iuh.fit.readhub.models.CommentDiscussionLike;
import com.iuh.fit.readhub.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

public interface CommentDiscussionLikeRepository extends JpaRepository<CommentDiscussionLike, Long> {
    boolean existsByCommentAndUser(Comment comment, User user);
    void deleteByCommentAndUser(Comment comment, User user);
    long countByComment(Comment comment);
    @Modifying
    @Query("DELETE FROM CommentDiscussionLike l WHERE l.comment = ?1")
    void deleteByComment(Comment comment);
}
