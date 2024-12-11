package com.iuh.fit.readhub.dto;

import com.iuh.fit.readhub.models.UserRole;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class UserManagementDTO {
    private Long userId;
    private String email;
    private String username;
    private String fullName;
    private String urlAvatar;
    private UserRole role;

    private Boolean forumInteractionBanned;
    private String forumBanReason;
    private LocalDateTime forumBanExpiresAt;

    private Boolean forumCreationBanned;
    private String forumCreationBanReason;
    private LocalDateTime forumCreationBanExpiresAt;

    private Boolean forumCommentBanned;
    private LocalDateTime forumCommentBanExpiresAt;

    private Boolean forumJoinBanned;
    private LocalDateTime forumJoinBanExpiresAt;
}