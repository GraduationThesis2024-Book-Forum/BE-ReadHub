package com.iuh.fit.readhub.dto.request;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Data
public class ForumRequest {
    private Long bookId;
    private String bookTitle;
    private String authors;
    private String forumTitle;
    private String forumDescription;
    private MultipartFile forumImage;
    private List<String> subjects;
    private List<String> categories;
}