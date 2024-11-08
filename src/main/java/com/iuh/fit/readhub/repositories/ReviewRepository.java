package com.iuh.fit.readhub.repositories;

import com.iuh.fit.readhub.models.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface ReviewRepository extends JpaRepository<Review, Long> {
    List<Review> findByUser_UserIdAndBookId(Long userId, Long bookId);
    List<Review> findByBookId(Long bookId);
}