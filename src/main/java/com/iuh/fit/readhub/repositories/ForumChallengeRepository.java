package com.iuh.fit.readhub.repositories;

import com.iuh.fit.readhub.constants.ChallengeType;
import com.iuh.fit.readhub.models.ForumChallenge;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ForumChallengeRepository extends JpaRepository<ForumChallenge, Long> {
    List<ForumChallenge> findAllByOrderByCreatedAtDesc();
    List<ForumChallenge> findByType(ChallengeType type);
}