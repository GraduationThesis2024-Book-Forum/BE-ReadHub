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
public class ChallengeMemberDTO {
    private Long id;
    private UserDTO user;
    private LocalDateTime joinedAt;
    private boolean completed;
    private LocalDateTime completedAt;
}