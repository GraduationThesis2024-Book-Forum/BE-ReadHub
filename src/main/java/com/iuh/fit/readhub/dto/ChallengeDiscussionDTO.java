package com.iuh.fit.readhub.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChallengeDiscussionDTO {
    private Long id;
    private String title;
    private String content;
    private UserDTO creator;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}