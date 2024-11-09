package com.iuh.fit.readhub.repositories;

import com.iuh.fit.readhub.models.Discussion;
import com.iuh.fit.readhub.models.ForumLike;
import com.iuh.fit.readhub.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

public interface ForumLikeRepository extends JpaRepository<ForumLike, Long> {
    boolean existsByDiscussionAndUser(Discussion discussion, User user);
    @Modifying
    @Query("DELETE FROM ForumLike fl WHERE fl.discussion = ?1 AND fl.user = ?2")
    void deleteByDiscussionAndUser(Discussion discussion, User user);
    long countByDiscussion(Discussion discussion);
}