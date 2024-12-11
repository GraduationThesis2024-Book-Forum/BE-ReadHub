package com.iuh.fit.readhub.models;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "challenge_members", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"challenge_id", "user_id"})
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChallengeMember {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "challenge_id", nullable = false)
    private ForumChallenge challenge;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    private LocalDateTime joinedAt;
    private boolean completed;
    private LocalDateTime completedAt;

    @Column(name = "reward_earned")
    private Boolean rewardEarned = false;

    @Column(name = "reward_type")
    private String rewardType;

    @Column(name = "reward_earned_at")
    private LocalDateTime rewardEarnedAt;
}