package com.iuh.fit.readhub.dto;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class CommentDiscussionReplyDTO {
    private Long id;
    private Long parentCommentId;
    private String content;
    private String imageUrl;
    private UserDTO user;
    private LocalDateTime createdAt;
}
