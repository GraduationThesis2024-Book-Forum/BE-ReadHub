package com.iuh.fit.readhub.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class CommentDTO {
    private Long id;
    private String content;
    private Long discussionId;
    private UserDTO user;
    private LocalDateTime createdAt;
}