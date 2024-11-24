package com.iuh.fit.readhub.models;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "challenge_discussions")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChallengeDiscussion {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "challenge_id")
    private ForumChallenge challenge;

    @ManyToOne
    @JoinColumn(name = "creator_id")
    private User creator;

    private String title;

    @Column(length = 1000)
    private String content;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}