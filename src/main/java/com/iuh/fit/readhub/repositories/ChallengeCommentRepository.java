package com.iuh.fit.readhub.repositories;

import com.iuh.fit.readhub.models.ChallengeComment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ChallengeCommentRepository extends JpaRepository<ChallengeComment, Long> {
}