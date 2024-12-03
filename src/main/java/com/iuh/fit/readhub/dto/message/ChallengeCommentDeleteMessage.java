package com.iuh.fit.readhub.dto.message;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ChallengeCommentDeleteMessage {
    private Long commentId;
    private Long challengeId;
}