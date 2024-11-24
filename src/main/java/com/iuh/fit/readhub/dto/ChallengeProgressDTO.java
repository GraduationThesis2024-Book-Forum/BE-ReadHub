package com.iuh.fit.readhub.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChallengeProgressDTO {
    private Long challengeId;
    private Long userId;
    private int totalProgress; // e.g., number of books read
    private int targetGoal; // e.g., target number of books
    private double progressPercentage;
    private boolean isCompleted;
}