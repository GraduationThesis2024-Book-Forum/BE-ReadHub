package com.iuh.fit.readhub.dto;

import com.iuh.fit.readhub.models.Review;
import com.iuh.fit.readhub.models.ReviewReact;
import com.iuh.fit.readhub.repositories.ReviewReactRepository;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReviewDTO {
    private Long reviewId;
    private Long userId;
    private String fullname;
    private Integer rating;
    private String review;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private int likeCount;
    private boolean isLiked = false;
    private boolean isReported = false;

    public static ReviewDTO fromReview(Review review, Long currentUserId) {
        ReviewDTO reviewDTO = new ReviewDTO();
        reviewDTO.reviewId = review.getReviewId();
        reviewDTO.userId = review.getUser().getUserId();
        reviewDTO.fullname = review.getUser().getFullName();
        reviewDTO.rating = review.getRating();
        reviewDTO.review = review.getReview();
        reviewDTO.createdAt = review.getCreatedAt();
        reviewDTO.updatedAt = review.getUpdatedAt();

        if (review.getReviewReacts() != null) {
            reviewDTO.likeCount = (int) review.getReviewReacts().stream()
                    .filter(ReviewReact::isLike)
                    .count();

            if (currentUserId != null) {
                review.getReviewReacts().stream()
                        .filter(react -> react.getUser().getUserId().equals(currentUserId))
                        .findFirst()
                        .ifPresent(userReact -> {
                            reviewDTO.isLiked = userReact.isLike();
                            reviewDTO.isReported = userReact.isReport();
                        });
            }
        }

        return reviewDTO;
    }

    public static List<ReviewDTO> fromReviews(List<Review> reviews, Long currentUserId) {
        return reviews.stream()
                .map(review -> fromReview(review, currentUserId))
                .toList();
    }
}