package com.iuh.fit.readhub.services;

import com.iuh.fit.readhub.models.ChallengeMember;
import com.iuh.fit.readhub.models.ForumChallenge;
import com.iuh.fit.readhub.repositories.ChallengeMemberRepository;
import com.iuh.fit.readhub.repositories.ReadingHistoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
@RequiredArgsConstructor
public class ChallengeProgressService {
    private final ChallengeMemberRepository challegeMemberRepository;
    private final ReadingHistoryRepository readingHistoryRepository;
    private final FCMService fcmService;
    private final UserService userService;

    @Transactional
    public void checkChallengeProgress(Long userId) {
        log.info("Checking challenge progress for user: {}", userId);

        List<ChallengeMember> activeMembers = challegeMemberRepository
                .findByUser_UserIdAndRewardEarnedFalseAndChallenge_EndDateAfter(
                        userId,
                        LocalDateTime.now()
                );

        log.info("Found {} active challenges for user", activeMembers.size());

        for (ChallengeMember member : activeMembers) {
            ForumChallenge challenge = member.getChallenge();
            log.info("Checking challenge: {}, Target books: {}",
                    challenge.getTitle(), challenge.getTargetBooks());

            long booksReadInPeriod = readingHistoryRepository.countDistinctBooksByUserAndDateRange(
                    userId,
                    challenge.getStartDate(),
                    challenge.getEndDate()
            );

            log.info("User has read {} books in period {} to {}",
                    booksReadInPeriod,
                    challenge.getStartDate(),
                    challenge.getEndDate());

            if (booksReadInPeriod >= challenge.getTargetBooks()) {
                log.info("Challenge completed! Updating reward status");
                member.setRewardEarned(true);
                member.setRewardType(challenge.getReward());
                member.setRewardEarnedAt(LocalDateTime.now());
                challegeMemberRepository.save(member);

                sendRewardNotification(member);
            } else {
                log.info("Challenge not completed yet. Need {} more books",
                        challenge.getTargetBooks() - booksReadInPeriod);
            }
        }
    }
    private void sendRewardNotification(ChallengeMember member) {
        try {
            Map<String, String> data = Map.of(
                    "type", "CHALLENGE_COMPLETED",
                    "challengeId", member.getChallenge().getChallengeId().toString(),
                    "rewardType", member.getRewardType()
            );

            log.info("Sending completion notification to user: {}",
                    member.getUser().getUserId());

            fcmService.sendNotification(
                    member.getUser().getUserId(),
                    "Challenge Completed!",
                    String.format("Congratulations! You've completed the '%s' challenge and earned the %s reward!",
                            member.getChallenge().getTitle(),
                            member.getRewardType()),
                    data
            );

            log.info("Notification sent successfully");
        } catch (Exception e) {
            log.error("Failed to send notification: ", e);
        }
    }
}