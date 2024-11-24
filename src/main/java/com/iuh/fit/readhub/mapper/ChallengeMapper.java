package com.iuh.fit.readhub.mapper;

import com.iuh.fit.readhub.dto.ChallengeDTO;
import com.iuh.fit.readhub.dto.ChallengeMemberDTO;
import com.iuh.fit.readhub.dto.ChallengeDiscussionDTO;
import com.iuh.fit.readhub.dto.request.CreateChallengeRequest;
import com.iuh.fit.readhub.models.ForumChallenge;
import com.iuh.fit.readhub.models.ChallengeMember;
import com.iuh.fit.readhub.models.ChallengeDiscussion;
import com.iuh.fit.readhub.models.User;
import org.springframework.stereotype.Component;
import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class ChallengeMapper {
    private final UserMapper userMapper;

    public ChallengeDTO toDTO(ForumChallenge challenge) {
        boolean isExpired = challenge.getEndDate().isBefore(LocalDateTime.now());

        return ChallengeDTO.builder()
                .challengeId(challenge.getChallengeId())
                .title(challenge.getTitle())
                .description(challenge.getDescription())
                .type(challenge.getType())
                // Cho READING_CHALLENGE
                .seasonOrMonth(challenge.getSeasonOrMonth())
                .selectedPeriod(challenge.getSelectedPeriod())
                .targetBooks(challenge.getTargetBooks())
                // Cho BOOK_CLUB
                .maxMembers(challenge.getMaxMembers())
                .isExpired(isExpired)
                // Các trường chung
                .startDate(challenge.getStartDate())
                .endDate(challenge.getEndDate())
                .reward(challenge.getReward())
                .creator(userMapper.toDTO(challenge.getCreator()))
                .memberCount(challenge.getMembers().size())
                .discussionCount(challenge.getDiscussions().size())
                .createdAt(challenge.getCreatedAt())
                .build();
    }

    // Overload để thêm thông tin isJoined cho user hiện tại
    public ChallengeDTO toDTO(ForumChallenge challenge, User currentUser) {
        ChallengeDTO dto = toDTO(challenge);
        if (currentUser != null) {
            dto.setJoined(challenge.getMembers().stream()
                    .anyMatch(member -> member.getUser().getUserId().equals(currentUser.getUserId())));
        }
        return dto;
    }

    public ForumChallenge toEntity(CreateChallengeRequest request) {
        return ForumChallenge.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .type(request.getType())
                // Cho READING_CHALLENGE
                .seasonOrMonth(request.getSeasonOrMonth())
                .selectedPeriod(request.getSelectedPeriod())
                .targetBooks(request.getTargetBooks())
                // Cho BOOK_CLUB
                .maxMembers(request.getMaxMembers())
                // Các trường chung
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .reward(request.getReward())
                .build();
    }
}