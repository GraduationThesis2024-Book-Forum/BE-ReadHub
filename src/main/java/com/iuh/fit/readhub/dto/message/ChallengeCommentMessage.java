package com.iuh.fit.readhub.dto.message;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class ChallengeCommentMessage {
    private Long challengeId;
    private String content;
    private List<Long> bookIds;
}