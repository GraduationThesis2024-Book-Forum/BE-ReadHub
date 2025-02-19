package com.iuh.fit.readhub.repositories;

import com.iuh.fit.readhub.models.ChallengeMember;
import com.iuh.fit.readhub.models.ForumChallenge;
import com.iuh.fit.readhub.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ChallengeMemberRepository extends JpaRepository<ChallengeMember, Long> {
    Optional<ChallengeMember> findByChallengeAndUser(ForumChallenge challenge, User user);

    @Query("SELECT cm FROM ChallengeMember cm WHERE cm.challenge.challengeId = :challengeId AND cm.user.userId = :userId")
    Optional<ChallengeMember> findByChallengeIdAndUserId(@Param("challengeId") Long challengeId, @Param("userId") Long userId);

    List<ChallengeMember> findByUser_UserIdAndRewardEarnedTrue(Long userId);

    @Query("SELECT cm FROM ChallengeMember cm " +
            "JOIN FETCH cm.challenge c " +
            "WHERE cm.user.userId = :userId " +
            "AND (cm.rewardEarned IS FALSE OR cm.rewardEarned IS NULL) " + // Sửa điều kiện này
            "AND c.endDate > :currentDate")
    List<ChallengeMember> findByUser_UserIdAndRewardEarnedFalseAndChallenge_EndDateAfter(
            @Param("userId") Long userId,
            @Param("currentDate") LocalDateTime currentDate
    );
}