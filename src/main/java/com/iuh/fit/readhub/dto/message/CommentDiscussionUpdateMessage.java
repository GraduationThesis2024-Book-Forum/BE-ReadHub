package com.iuh.fit.readhub.dto.message;

import lombok.Data;

@Data
public class CommentDiscussionUpdateMessage {
    private Long commentId;
    private String content;
    private String imageUrl;
}
