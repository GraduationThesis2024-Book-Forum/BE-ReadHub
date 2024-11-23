package com.iuh.fit.readhub.dto;

import lombok.Data;
import lombok.Builder;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DiscussionDTO {
    private Long discussionId;
    private String forumTitle;
    private String forumDescription;
    private String imageUrl;
    private String bookTitle;
    private String authors;
    private List<String> subjects;
    private List<String> categories;
    private UserDTO creator;
    private int totalMembers;
    private int totalPosts;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private boolean trending;
    private List<String> recentTopics;
}
