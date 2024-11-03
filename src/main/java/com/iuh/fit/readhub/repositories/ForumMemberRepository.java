package com.iuh.fit.readhub.repositories;

import com.iuh.fit.readhub.models.ForumMember;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface ForumMemberRepository extends JpaRepository<ForumMember, Long> {
    Optional<ForumMember> findByDiscussion_DiscussionIdAndUser_UserId(Long discussionId, Long userId);
    boolean existsByDiscussion_DiscussionIdAndUser_UserId(Long discussionId, Long userId);
}