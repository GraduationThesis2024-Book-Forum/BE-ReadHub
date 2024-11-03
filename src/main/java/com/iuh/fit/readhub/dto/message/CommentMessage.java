package com.iuh.fit.readhub.dto.message;

import lombok.Data;

@Data
public class CommentMessage {
    private String content;
    private Long discussionId;
}