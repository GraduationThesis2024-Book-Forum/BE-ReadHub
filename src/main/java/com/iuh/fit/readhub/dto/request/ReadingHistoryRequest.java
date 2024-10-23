package com.iuh.fit.readhub.dto.request;

import lombok.Data;

@Data
public class ReadingHistoryRequest {
    private Long readingHistoryId;
    private Long userId;
    private Long bookId;
    private Integer timeSpent;
}
