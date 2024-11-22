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

    // Add ban information
    private boolean forumInteractionBanned;
    private String forumBanReason;
    private LocalDateTime forumBanExpiresAt;

    // Helper method for FE to check ban status
    @JsonProperty("isCurrentlyBanned")
    public boolean isCurrentlyBanned() {
        if (!forumInteractionBanned) return false;
        if (forumBanExpiresAt == null) return true; // Permanent ban
        return LocalDateTime.now().isBefore(forumBanExpiresAt);
    }

    // Helper method for FE to get remaining ban duration
    @JsonProperty("banDurationRemaining")
    public String getBanDurationRemaining() {
        if (!forumInteractionBanned) return null;
        if (forumBanExpiresAt == null) return "PERMANENT";
        if (LocalDateTime.now().isAfter(forumBanExpiresAt)) return null;

        Duration duration = Duration.between(LocalDateTime.now(), forumBanExpiresAt);
        long hours = duration.toHours();
        return hours + " hours";
    }
}
