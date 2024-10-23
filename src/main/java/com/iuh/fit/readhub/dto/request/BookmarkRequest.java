package com.iuh.fit.readhub.dto.request;

import lombok.Data;

@Data
public class BookmarkRequest {
    private Long bookmarkId;
    private Long userId;
    private Long bookId;
    private String location;
}
