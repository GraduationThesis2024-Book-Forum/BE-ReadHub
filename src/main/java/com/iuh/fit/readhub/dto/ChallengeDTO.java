package com.iuh.fit.readhub.dto;

import com.iuh.fit.readhub.constants.ChallengeType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChallengeDTO {
    private Long challengeId;
    private String title;
    private String description;
    private ChallengeType type;
    private String seasonOrMonth;
    private String selectedPeriod;
    private Integer targetBooks;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private String reward;
    private UserDTO creator;
    private int memberCount;
    private int discussionCount;
    private boolean isJoined;
    private LocalDateTime createdAt;
}