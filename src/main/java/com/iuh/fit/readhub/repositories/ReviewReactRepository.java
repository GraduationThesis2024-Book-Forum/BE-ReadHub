package com.iuh.fit.readhub.repositories;

import com.iuh.fit.readhub.models.ReviewReact;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ReviewReactRepository extends JpaRepository<ReviewReact, Long> {
//    findByReviewAndUserUserId
    Optional<ReviewReact> findByReview_ReviewIdAndUser_UserId(Long reviewId, Long userId);

}