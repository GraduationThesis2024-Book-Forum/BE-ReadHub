package com.iuh.fit.readhub.dto.request;

import com.iuh.fit.readhub.constants.ChallengeType;
import lombok.Builder;
import lombok.Data;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

@Data
@Builder
public class CreateChallengeRequest {
    @NotBlank(message = "Title is required")
    private String title;

    @NotBlank(message = "Description is required")
    private String description;

    @NotNull(message = "Type is required")
    private ChallengeType type;

    // Cho READING_CHALLENGE
    private String seasonOrMonth; // 'SEASON' hoặc 'MONTH'
    private String selectedPeriod; // Tên mùa hoặc tháng
    private Integer targetBooks;

    // Cho BOOK_CLUB
    private Integer maxMembers;

    @NotNull(message = "Start date is required")
    private LocalDateTime startDate;

    @NotNull(message = "End date is required")
    private LocalDateTime endDate;

    private String reward;
}
