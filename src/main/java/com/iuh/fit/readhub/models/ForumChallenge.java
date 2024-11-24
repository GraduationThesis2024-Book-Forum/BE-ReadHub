package com.iuh.fit.readhub.models;

import com.iuh.fit.readhub.constants.ChallengeType;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "forum_challenges")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ForumChallenge {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long challengeId;

    @Column(nullable = false)
    private String title;

    @Column(length = 1000)
    private String description;

    @Enumerated(EnumType.STRING)
    private ChallengeType type;

    // Cho READING_CHALLENGE
    private String seasonOrMonth; // 'SEASON' hoặc 'MONTH'
    private String selectedPeriod; // Tên mùa hoặc tháng
    private Integer targetBooks; // Số sách cần đọc

    // Cho BOOK_CLUB
    private Integer maxMembers; // Giới hạn thành viên

    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private String reward;

    @ManyToOne
    @JoinColumn(name = "creator_id")
    private User creator;

    @OneToMany(mappedBy = "challenge", cascade = CascadeType.ALL)
    private Set<ChallengeMember> members = new HashSet<>();

    @OneToMany(mappedBy = "challenge")
    private Set<ChallengeDiscussion> discussions = new HashSet<>();

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
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