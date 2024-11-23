package com.iuh.fit.readhub.repositories;

import com.iuh.fit.readhub.models.Discussion;
import com.iuh.fit.readhub.models.DiscussionMember;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.Optional;

public interface DiscussionMemberRepository extends JpaRepository<DiscussionMember, Long> {
    Optional<DiscussionMember> findByDiscussion_DiscussionIdAndUser_UserId(Long discussionId, Long userId);

    boolean existsByDiscussion_DiscussionIdAndUser_UserId(Long discussionId, Long userId);

    long countByDiscussion_DiscussionId(Long discussionId);

    // Hoặc có thể viết query tường minh hơn
    @Query("SELECT COUNT(fm) FROM DiscussionMember fm WHERE fm.discussion.discussionId = :discussionId")
    long getDiscussionMemberCount(@Param("discussionId") Long discussionId);

    // Thêm một số method hữu ích khác
    @Query("SELECT CASE WHEN COUNT(fm) > 0 THEN true ELSE false END FROM DiscussionMember fm " +
            "WHERE fm.discussion.discussionId = :discussionId AND fm.user.userId = :userId")
    boolean checkMembership(@Param("discussionId") Long discussionId, @Param("userId") Long userId);

    @Modifying
    @Query("DELETE FROM DiscussionMember fm WHERE fm.discussion = ?1")
    void deleteByDiscussion(Discussion discussion);
}