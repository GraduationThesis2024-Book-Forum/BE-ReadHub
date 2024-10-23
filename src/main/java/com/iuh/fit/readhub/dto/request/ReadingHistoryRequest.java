package com.iuh.fit.readhub.dto.request;

import lombok.Data;

@Data
public class ReadingHistoryRequest {
    private Long userId;
    private Long bookId;
}
