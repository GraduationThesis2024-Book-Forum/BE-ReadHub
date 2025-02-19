package com.iuh.fit.readhub.dto;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class CommentDTO {
    private Long id;
    private String content;
    private String imageUrl;
    private Long discussionId;
    private UserDTO user;
    private LocalDateTime createdAt;
    private int likeCount;
    private boolean isLikedByCurrentUser;
    private List<CommentDiscussionReplyDTO> replies;
}