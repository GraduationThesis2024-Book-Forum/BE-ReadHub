package com.iuh.fit.readhub.services;

import com.iuh.fit.readhub.constants.ChallengeType;
import com.iuh.fit.readhub.dto.ChallengeDTO;
import com.iuh.fit.readhub.dto.request.CreateChallengeRequest;
import com.iuh.fit.readhub.mapper.UserMapper;
import com.iuh.fit.readhub.models.ChallengeMember;
import com.iuh.fit.readhub.models.ForumChallenge;
import com.iuh.fit.readhub.models.User;
import com.iuh.fit.readhub.repositories.ForumChallengeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import jakarta.transaction.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ForumChallengeService {
    private final ForumChallengeRepository challengeRepository;
    private final UserService userService;
    private final UserMapper userMapper;

    public List<ChallengeDTO> getAllChallenges() {
        return challengeRepository.findAllByOrderByCreatedAtDesc()
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public ChallengeDTO createChallenge(CreateChallengeRequest request, Authentication authentication) {
        User creator = userService.getCurrentUser(authentication);

        validateChallengeRequest(request);

        ForumChallenge challenge = ForumChallenge.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .type(request.getType())
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .reward(request.getReward())
                .creator(creator)
                .build();

        if (request.getType() == ChallengeType.READING_CHALLENGE) {
            challenge.setSeasonOrMonth(request.getSeasonOrMonth());
            challenge.setSelectedPeriod(request.getSelectedPeriod());
            challenge.setTargetBooks(request.getTargetBooks());
        } else if (request.getType() == ChallengeType.BOOK_CLUB) {
            challenge.setMaxMembers(request.getMaxMembers());
        }

        return convertToDTO(challengeRepository.save(challenge));
    }


    @Transactional
    public ChallengeDTO joinChallenge(Long challengeId, Authentication authentication) {
        User user = userService.getCurrentUser(authentication);
        ForumChallenge challenge = challengeRepository.findById(challengeId)
                .orElseThrow(() -> new RuntimeException("Challenge not found"));

        // Kiểm tra các điều kiện
        if (challenge.getEndDate().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("Challenge has ended");
        }

        if (challenge.getType() == ChallengeType.BOOK_CLUB) {
            if (challenge.getMembers().size() >= challenge.getMaxMembers()) {
                throw new RuntimeException("Book club is full");
            }
        }

        if (challenge.getMembers().stream()
                .anyMatch(member -> member.getUser().getUserId().equals(user.getUserId()))) {
            throw new RuntimeException("Already joined this challenge");
        }

        ChallengeMember member = ChallengeMember.builder()
                .challenge(challenge)
                .user(user)
                .build();

        challenge.getMembers().add(member);
        return convertToDTO(challengeRepository.save(challenge));
    }

    private ChallengeDTO convertToDTO(ForumChallenge challenge) {
        boolean isExpired = challenge.getEndDate().isBefore(LocalDateTime.now());

        return ChallengeDTO.builder()
                .challengeId(challenge.getChallengeId())
                .title(challenge.getTitle())
                .description(challenge.getDescription())
                .type(challenge.getType())
                .seasonOrMonth(challenge.getSeasonOrMonth())
                .selectedPeriod(challenge.getSelectedPeriod())
                .targetBooks(challenge.getTargetBooks())
                .maxMembers(challenge.getMaxMembers())
                .startDate(challenge.getStartDate())
                .endDate(challenge.getEndDate())
                .reward(challenge.getReward())
                .creator(userMapper.toDTO(challenge.getCreator()))
                .memberCount(challenge.getMembers().size())
                .discussionCount(challenge.getDiscussions().size())
                .isExpired(isExpired)
                .createdAt(challenge.getCreatedAt())
                .build();
    }

    private void validateChallengeRequest(CreateChallengeRequest request) {
        if (request.getStartDate().isAfter(request.getEndDate())) {
            throw new RuntimeException("Start date must be before end date");
        }

        if (request.getType() == ChallengeType.READING_CHALLENGE) {
            if (request.getTargetBooks() == null || request.getTargetBooks() <= 0) {
                throw new RuntimeException("Invalid target books number");
            }
        } else if (request.getType() == ChallengeType.BOOK_CLUB) {
            if (request.getMaxMembers() == null || request.getMaxMembers() < 2) {
                throw new RuntimeException("Book club must allow at least 2 members");
            }
        }
    }
}