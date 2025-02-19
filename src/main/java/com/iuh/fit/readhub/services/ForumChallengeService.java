package com.iuh.fit.readhub.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.iuh.fit.readhub.constants.ChallengeType;
import com.iuh.fit.readhub.dto.ChallengeCommentDTO;
import com.iuh.fit.readhub.dto.ChallengeDTO;
import com.iuh.fit.readhub.dto.GutendexBookDTO;
import com.iuh.fit.readhub.dto.request.CreateChallengeRequest;
import com.iuh.fit.readhub.mapper.UserMapper;
import com.iuh.fit.readhub.models.ChallengeComment;
import com.iuh.fit.readhub.models.ChallengeMember;
import com.iuh.fit.readhub.models.ForumChallenge;
import com.iuh.fit.readhub.models.User;
import com.iuh.fit.readhub.repositories.ChallengeCommentRepository;
import com.iuh.fit.readhub.repositories.ChallengeMemberRepository;
import com.iuh.fit.readhub.repositories.ForumChallengeRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import jakarta.transaction.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class ForumChallengeService {
    private final ForumChallengeRepository challengeRepository;
    private final UserService userService;
    private final UserMapper userMapper;
    private final ChallengeMemberRepository challengeMemberRepository;
    private final ChallengeCommentRepository commentRepository;
    private final ObjectMapper objectMapper;

    public List<ChallengeDTO> getAllChallenges() {
        return challengeRepository.findAllByOrderByCreatedAtDesc()
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public ChallengeDTO getForumById(Long challengeId) {
        return challengeRepository.findById(challengeId)
                .map(this::convertToDTO)
                .orElseThrow(() -> new EntityNotFoundException("Challenge not found: " + challengeId));
    }

    public List<ChallengeCommentDTO> getCommentsByChallengeId(Long challengeId, Authentication authentication) {
        User currentUser = userService.getCurrentUser(authentication);
        List<ChallengeComment> comments = commentRepository.findByChallenge_ChallengeIdOrderByCreatedAtDesc(challengeId);
        return comments.stream()
                .map(comment -> convertToDTO(comment, currentUser))
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
        }

        return convertToDTO(challengeRepository.save(challenge));
    }


    @Transactional
    public ChallengeDTO joinChallenge(Long challengeId, Authentication authentication) {
        log.info("Attempting to join challenge: {}", challengeId);

        User user = userService.getCurrentUser(authentication);
        ForumChallenge challenge = challengeRepository.findById(challengeId)
                .orElseThrow(() -> new EntityNotFoundException("Challenge not found: " + challengeId));

        log.info("Found challenge: {}, user: {}", challenge.getTitle(), user.getUserId());

        if (challenge.getEndDate().isBefore(LocalDateTime.now())) {
            throw new IllegalStateException("Challenge has ended");
        }

        Optional<ChallengeMember> existingMember = challengeMemberRepository
                .findByChallengeIdAndUserId(challengeId, user.getUserId());

        if (existingMember.isPresent()) {
            throw new IllegalStateException("User already joined this challenge");
        }

        try {
            ChallengeMember member = ChallengeMember.builder()
                    .challenge(challenge)
                    .user(user)
                    .joinedAt(LocalDateTime.now())
                    .completed(false)
                    .build();

            member = challengeMemberRepository.save(member);
            log.info("Saved new member: {}", member.getId());

            return convertToDTO(challenge);
        } catch (Exception e) {
            log.error("Error joining challenge:", e);
            throw new RuntimeException("Error joining challenge: " + e.getMessage());
        }
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
                .startDate(challenge.getStartDate())
                .endDate(challenge.getEndDate())
                .reward(challenge.getReward())
                .creator(userMapper.toDTO(challenge.getCreator()))
                .memberCount(challenge.getMembers().size())
                .discussionCount(challenge.getDiscussions().size())
                .createdAt(challenge.getCreatedAt())
                .build();
    }

    private ChallengeCommentDTO convertToDTO(ChallengeComment comment, User currentUser) {
        List<GutendexBookDTO> books = new ArrayList<>();
        if (comment.getBooksJson() != null) {
            try {
                books = objectMapper.readValue(
                        comment.getBooksJson(),
                        objectMapper.getTypeFactory().constructCollectionType(List.class, GutendexBookDTO.class)
                );
            } catch (JsonProcessingException e) {
                log.error("Error parsing books JSON", e);
            }
        }

        // Lấy thông tin reward từ challenge_members
        ChallengeMember member = challengeMemberRepository
                .findByChallengeAndUser(comment.getChallenge(), comment.getUser())
                .orElse(null);

        boolean hasReward = member != null && member.getRewardEarned() != null && member.getRewardEarned();

        return ChallengeCommentDTO.builder()
                .id(comment.getId())
                .content(comment.getContent())
                .imageUrl(comment.getImageUrl())
                .user(userMapper.toDTO(comment.getUser()))
                .books(books)
                .createdAt(comment.getCreatedAt())
                .isOwner(comment.getUser().equals(currentUser))
                .rewardEarned(hasReward)
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
        }
    }
}