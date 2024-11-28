package com.iuh.fit.readhub.models;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Entity
@Builder
@Table(name = "user", uniqueConstraints = {
        @UniqueConstraint(columnNames = "email"),
        @UniqueConstraint(columnNames = "username")
})
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long userId;

    @Column(unique = true, nullable = false, length = 50)
    private String username;

    @Column(name = "full_name", length = 100)
    private String fullName;

    @Column(nullable = false)
    private String password;

    @Column(unique = true, nullable = false, length = 100)
    private String email;

    @Column(name = "url_avatar")
    private String urlAvatar;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserRole role;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Set<Note> notes = new HashSet<>();

    @OneToMany(mappedBy = "creator", cascade = CascadeType.ALL, orphanRemoval = true)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Set<Discussion> discussions = new HashSet<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Set<ReadingHistory> readingHistories = new HashSet<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Set<Review> reviews = new HashSet<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Set<SavedBook> savedBooks = new HashSet<>();

    @Column(name = "forum_creation_banned", nullable = false)
    private Boolean forumCreationBanned = false;

    @Column(name = "forum_creation_ban_reason", length = 500)
    private String forumCreationBanReason;

    @Column(name = "forum_creation_ban_expires_at")
    private LocalDateTime forumCreationBanExpiresAt;

    @Column(name = "forum_interaction_banned", nullable = false)
    private Boolean forumInteractionBanned = false;

    @Column(name = "forum_ban_reason", length = 500)
    private String forumBanReason;

    @Column(name = "forum_ban_expires_at")
    private LocalDateTime forumBanExpiresAt;

    @Column(name = "forum_comment_banned", nullable = false)
    private Boolean forumCommentBanned = false;

    @Column(name = "forum_comment_ban_expires_at")
    private LocalDateTime forumCommentBanExpiresAt;

    @Column(name = "forum_join_banned", nullable = false)
    private Boolean forumJoinBanned = false;

    @Column(name = "forum_join_ban_expires_at")
    private LocalDateTime forumJoinBanExpiresAt;

    @PreUpdate
    private void checkBanExpiry() {
        LocalDateTime now = LocalDateTime.now();

        // Check forum interaction ban
        if (Boolean.TRUE.equals(forumInteractionBanned) && forumBanExpiresAt != null
                && forumBanExpiresAt.isBefore(now)) {
            forumInteractionBanned = false;
            forumBanReason = null;
            forumBanExpiresAt = null;
        }

        // Check forum comment ban
        if (Boolean.TRUE.equals(forumCommentBanned) && forumCommentBanExpiresAt != null
                && forumCommentBanExpiresAt.isBefore(now)) {
            forumCommentBanned = false;
            forumCommentBanExpiresAt = null;
        }

        // Check forum join ban
        if (Boolean.TRUE.equals(forumJoinBanned) && forumJoinBanExpiresAt != null
                && forumJoinBanExpiresAt.isBefore(now)) {
            forumJoinBanned = false;
            forumJoinBanExpiresAt = null;
        }

        // Check forum creation ban
        if (Boolean.TRUE.equals(forumCreationBanned) && forumCreationBanExpiresAt != null
                && forumCreationBanExpiresAt.isBefore(now)) {
            forumCreationBanned = false;
            forumCreationBanReason = null;
            forumCreationBanExpiresAt = null;
        }
    }

    public boolean isCurrentlyBanned() {
        checkBanExpiry();
        return Boolean.TRUE.equals(forumInteractionBanned) ||
                Boolean.TRUE.equals(forumCreationBanned) ||
                Boolean.TRUE.equals(forumCommentBanned) ||
                Boolean.TRUE.equals(forumJoinBanned);
    }

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        if (forumCreationBanned == null) {
            forumCreationBanned = false;
        }
        if (role == null) {
            role = UserRole.USER;
        }
    }

    public boolean isAdmin() {
        return UserRole.ADMIN.equals(role);
    }
}