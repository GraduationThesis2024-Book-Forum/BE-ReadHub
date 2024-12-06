package com.iuh.fit.readhub.dto.message;

import com.iuh.fit.readhub.dto.GutendexBookDTO;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class ChallengeCommentMessage {
    private Long challengeId;
    private String content;
    private String imageUrl;
    private List<GutendexBookDTO> books;  // Changed to use GutendexBookDTO
}