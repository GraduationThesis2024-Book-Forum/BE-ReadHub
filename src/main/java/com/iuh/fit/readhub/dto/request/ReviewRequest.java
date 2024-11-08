package com.iuh.fit.readhub.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReviewRequest {
    private Long reviewId;
    private Long bookId;
    private Long userId;
    private Integer rating;
    private String review;
}
