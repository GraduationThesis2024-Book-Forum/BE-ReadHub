package com.iuh.fit.readhub.services;

import com.iuh.fit.readhub.dto.ReviewDTO;
import com.iuh.fit.readhub.models.Review;
import com.iuh.fit.readhub.repositories.ReviewRepository;
import com.iuh.fit.readhub.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;


@Service
@RequiredArgsConstructor
public class ReviewService {
    @Autowired
    private ReviewRepository reviewRepository;

    @Autowired
    private UserRepository userRepository;

    public Map<String, Object> getBookReviews(Long bookId, Long currentUserId) {
        List<Review> reviews = reviewRepository.findByBookId(bookId);

        // Tính rating trung bình
        double averageRating = 0;
        if (!reviews.isEmpty()) {
            averageRating = reviews.stream()
                    .mapToInt(Review::getRating)
                    .average()
                    .orElse(0.0);
        }

        // Đếm số lượng mỗi rating
        Map<Integer, Integer> distribution = new HashMap<>();
        for (int i = 1; i <= 5; i++) {
            int finalI = i;
            int count = (int) reviews.stream()
                    .filter(r -> r.getRating() == finalI)
                    .count();
            distribution.put(i, count);
        }

        List<ReviewDTO> reviewsDTO = ReviewDTO.fromReviews(reviews, currentUserId);

        Map<String, Object> result = new HashMap<>();
        result.put("total", reviews.size());
        result.put("averageRating", Math.round(averageRating * 10.0) / 10.0);
        result.put("distribution", distribution);
        result.put("reviews", reviewsDTO);

        return result;
    }

    public List<Review> getUserReview(Long userId, Long bookId) {
        return reviewRepository.findByUser_UserIdAndBookId(userId, bookId);
    }

    public Review createReview(Review review) {
        review.setCreatedAt(LocalDateTime.now());
        review.setUpdatedAt(LocalDateTime.now());
        return reviewRepository.save(review);
    }

    public Review updateReview(Review review) {
        review.setUpdatedAt(LocalDateTime.now());
        return reviewRepository.save(review);
    }

    public void deleteReview(Long reviewId) {
        reviewRepository.deleteById(reviewId);
    }


    public double getAverageRating(Long bookId) {
        List<Review> reviews = reviewRepository.findByBookId(bookId);
        if (reviews.isEmpty()) {
            return 0;
        }
        return reviews.stream()
                .mapToInt(Review::getRating)
                .average()
                .orElse(0.0);
    }
}