package com.iuh.fit.readhub.dto;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class ChallengeCommentDTO {
    private Long id;
    private String content;
    private String imageUrl;  // Added for comment images
    private UserDTO user;
    private List<GutendexBookDTO> books;
    private LocalDateTime createdAt;
    private boolean isOwner;
}
