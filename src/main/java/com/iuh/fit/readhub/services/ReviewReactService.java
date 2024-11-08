package com.iuh.fit.readhub.services;

import com.iuh.fit.readhub.models.Review;
import com.iuh.fit.readhub.models.ReviewReact;
import com.iuh.fit.readhub.models.User;
import com.iuh.fit.readhub.repositories.ReviewReactRepository;
import com.iuh.fit.readhub.repositories.ReviewRepository;
import com.iuh.fit.readhub.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class ReviewReactService {
    private final ReviewRepository reviewRepository;
    private final ReviewReactRepository reviewReactRepository;
    private final UserRepository userRepository;

    public void likeReview(Long reviewId, Long userId) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new RuntimeException("Review not found"));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Tìm reaction hiện tại của user với review này
        ReviewReact reviewReact = reviewReactRepository
                .findByReview_ReviewIdAndUser_UserId(reviewId, userId)
                .orElse(null);

        if (reviewReact == null) {
            // Chưa có reaction nào -> tạo mới với isLike = true
            reviewReact = new ReviewReact();
            reviewReact.setReview(review);
            reviewReact.setUser(user);
            reviewReact.setLike(true);
            reviewReact.setReport(false);
            reviewReact.setCreatedAt(LocalDateTime.now());
            reviewReact.setUpdatedAt(LocalDateTime.now());
        } else {
            // Đã có reaction -> toggle trạng thái like
            reviewReact.setLike(!reviewReact.isLike());
            reviewReact.setUpdatedAt(LocalDateTime.now());
        }

        reviewReactRepository.save(reviewReact);
    }

    public void reportReview(Long reviewId, Long userId) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new RuntimeException("Review not found"));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Tìm reaction hiện tại của user với review này
        ReviewReact reviewReact = reviewReactRepository
                .findByReview_ReviewIdAndUser_UserId(reviewId, userId)
                .orElse(null);

        if (reviewReact == null) {
            // Chưa có reaction nào -> tạo mới với isReport = true
            reviewReact = new ReviewReact();
            reviewReact.setReview(review);
            reviewReact.setUser(user);
            reviewReact.setLike(false);
            reviewReact.setReport(true);
            reviewReact.setCreatedAt(LocalDateTime.now());
            reviewReact.setUpdatedAt(LocalDateTime.now());
        } else if (!reviewReact.isReport()) {
            // Đã có reaction nhưng chưa report -> set report = true
            reviewReact.setReport(true);
            reviewReact.setUpdatedAt(LocalDateTime.now());
        }
        // Nếu đã report rồi -> không làm gì cả (không thể un-report)
        reviewReactRepository.save(reviewReact);
    }
}
