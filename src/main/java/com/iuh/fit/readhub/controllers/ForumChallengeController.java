package com.iuh.fit.readhub.controllers;

import com.iuh.fit.readhub.dto.ApiResponse;
import com.iuh.fit.readhub.dto.ChallengeCommentDTO;
import com.iuh.fit.readhub.dto.ChallengeDTO;
import com.iuh.fit.readhub.dto.request.CreateChallengeRequest;
import com.iuh.fit.readhub.services.ChallengeMemberService;
import com.iuh.fit.readhub.services.ForumChallengeService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/challenges")
@RequiredArgsConstructor
public class ForumChallengeController {
    private final ForumChallengeService challengeService;
    private final ChallengeMemberService memberService;

    @GetMapping
    public ResponseEntity<ApiResponse<?>> getAllChallenges() {
        try {
            List<ChallengeDTO> challenges = challengeService.getAllChallenges();
            return ResponseEntity.ok(ApiResponse.builder()
                    .message("Challenges fetched successfully")
                    .status(200)
                    .data(challenges)
                    .success(true)
                    .build());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.builder()
                    .message("Error fetching challenges: " + e.getMessage())
                    .status(400)
                    .success(false)
                    .build());
        }
    }

    @GetMapping("/{challengeId}/comments")
    public ResponseEntity<ApiResponse<?>> getComments(
            @PathVariable Long challengeId,
            Authentication authentication
    ) {
        try {
            List<ChallengeCommentDTO> comments = challengeService.getCommentsByChallengeId(challengeId, authentication);
            return ResponseEntity.ok(ApiResponse.builder()
                    .message("Comments fetched successfully")
                    .status(200)
                    .data(comments)
                    .success(true)
                    .build());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.builder()
                    .message("Error fetching comments: " + e.getMessage())
                    .status(400)
                    .success(false)
                    .build());
        }
    }
    @GetMapping("/{challengeId}/check-membership")
    public ResponseEntity<ApiResponse<?>> checkMembership(
            @PathVariable Long challengeId,
            Authentication authentication) {
        try {
            boolean isMember = memberService.isMember(challengeId, authentication);
            return ResponseEntity.ok(ApiResponse.builder()
                    .message("Membership checked successfully")
                    .status(200)
                    .data(Map.of("isMember", isMember))
                    .success(true)
                    .build());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.builder()
                    .message("Error checking membership: " + e.getMessage())
                    .status(400)
                    .success(false)
                    .build());
        }
    }

    @PostMapping
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<ApiResponse<?>> createChallenge(
            @RequestBody CreateChallengeRequest request,
            Authentication authentication) {
        try {
            ChallengeDTO challenge = challengeService.createChallenge(request, authentication);
            return ResponseEntity.ok(ApiResponse.builder()
                    .message("Challenge created successfully")
                    .status(200)
                    .data(challenge)
                    .success(true)
                    .build());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.builder()
                    .message("Error creating challenge: " + e.getMessage())
                    .status(400)
                    .success(false)
                    .build());
        }
    }

    @PostMapping("/{challengeId}/join")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<?>> joinChallenge(
            @PathVariable Long challengeId,
            Authentication authentication) {
        try {
            ChallengeDTO challenge = challengeService.joinChallenge(challengeId, authentication);
            return ResponseEntity.ok(ApiResponse.builder()
                    .message("Successfully joined the challenge")
                    .status(200)
                    .data(challenge)
                    .success(true)
                    .build());
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(404).body(ApiResponse.builder()
                    .message(e.getMessage())
                    .status(404)
                    .success(false)
                    .build());
        } catch (IllegalStateException e) {
            return ResponseEntity.status(400).body(ApiResponse.builder()
                    .message(e.getMessage())
                    .status(400)
                    .success(false)
                    .build());
        } catch (Exception e) {
            return ResponseEntity.status(500).body(ApiResponse.builder()
                    .message("An unexpected error occurred: " + e.getMessage())
                    .status(500)
                    .success(false)
                    .build());
        }
    }
}