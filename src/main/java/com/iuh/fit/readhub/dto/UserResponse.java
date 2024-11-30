package com.iuh.fit.readhub.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.time.Duration;
import java.time.LocalDateTime;

@ToString
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserResponse {
    private Long userId;
    private String username;
    private String fullName;
    private String email;
    private String role;
    private String urlAvatar;

    // Forum interaction ban
    private boolean forumInteractionBanned;
    private String forumBanReason;
    private LocalDateTime forumBanExpiresAt;

    // Forum creation ban
    private boolean forumCreationBanned;
    private String forumCreationBanReason;
    private LocalDateTime forumCreationBanExpiresAt;

    // Forum comment ban
    private boolean forumCommentBanned;
    private LocalDateTime forumCommentBanExpiresAt;

    // Forum join ban
    private boolean forumJoinBanned;
    private LocalDateTime forumJoinBanExpiresAt;

    // Helper method for FE to check overall ban status
    @JsonProperty("isCurrentlyBanned")
    public boolean isCurrentlyBanned() {
        return isInteractionBanned() || isCreationBanned() ||
                isCommentBanned() || isJoinBanned();
    }

    // Helper methods for specific ban checks
    private boolean isInteractionBanned() {
        if (!forumInteractionBanned) return false;
        if (forumBanExpiresAt == null) return true; // Permanent ban
        return LocalDateTime.now().isBefore(forumBanExpiresAt);
    }

    private boolean isCreationBanned() {
        if (!forumCreationBanned) return false;
        if (forumCreationBanExpiresAt == null) return true; // Permanent ban
        return LocalDateTime.now().isBefore(forumCreationBanExpiresAt);
    }

    private boolean isCommentBanned() {
        if (!forumCommentBanned) return false;
        if (forumCommentBanExpiresAt == null) return true; // Permanent ban
        return LocalDateTime.now().isBefore(forumCommentBanExpiresAt);
    }

    private boolean isJoinBanned() {
        if (!forumJoinBanned) return false;
        if (forumJoinBanExpiresAt == null) return true; // Permanent ban
        return LocalDateTime.now().isBefore(forumJoinBanExpiresAt);
    }

    // Helper method for FE to get remaining ban duration
    @JsonProperty("banDurationRemaining")
    public String getBanDurationRemaining() {
        LocalDateTime latestExpiry = null;

        if (forumInteractionBanned && forumBanExpiresAt != null) {
            latestExpiry = forumBanExpiresAt;
        }
        if (forumCreationBanned && forumCreationBanExpiresAt != null) {
            if (latestExpiry == null || forumCreationBanExpiresAt.isAfter(latestExpiry)) {
                latestExpiry = forumCreationBanExpiresAt;
            }
        }
        if (forumCommentBanned && forumCommentBanExpiresAt != null) {
            if (latestExpiry == null || forumCommentBanExpiresAt.isAfter(latestExpiry)) {
                latestExpiry = forumCommentBanExpiresAt;
            }
        }
        if (forumJoinBanned && forumJoinBanExpiresAt != null) {
            if (latestExpiry == null || forumJoinBanExpiresAt.isAfter(latestExpiry)) {
                latestExpiry = forumJoinBanExpiresAt;
            }
        }

        if (latestExpiry == null) {
            if (isCurrentlyBanned()) return "PERMANENT";
            return null;
        }

        if (LocalDateTime.now().isAfter(latestExpiry)) return null;

        Duration duration = Duration.between(LocalDateTime.now(), latestExpiry);
        long hours = duration.toHours();
        return hours + " hours";
    }
}