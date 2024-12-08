package com.iuh.fit.readhub.repositories;

import com.iuh.fit.readhub.models.ChallengeComment;
import com.iuh.fit.readhub.models.ForumChallenge;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChallengeCommentRepository extends JpaRepository<ChallengeComment, Long> {
    List<ChallengeComment> findByChallengeOrderByCreatedAtDesc(ForumChallenge challenge);
    List<ChallengeComment> findByChallenge_ChallengeIdOrderByCreatedAtDesc(Long challengeId);
}