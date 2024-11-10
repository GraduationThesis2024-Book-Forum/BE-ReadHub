package com.iuh.fit.readhub.dto.message;

import lombok.Data;

@Data
public class CommentReplyDiscussionUpdateMessage {
    private Long replyId;
    private String content;
    private String imageUrl;
}
