package com.iuh.fit.readhub.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ForumInteractionDTO {
    private boolean isLiked;
    private boolean isSaved;
    private long likeCount;
    private long saveCount;
}