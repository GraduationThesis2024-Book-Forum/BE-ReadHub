package com.iuh.fit.readhub.dto.message;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
public class CommentMessage {
    private String content;
    private Long discussionId;
    private String imageUrl;
}