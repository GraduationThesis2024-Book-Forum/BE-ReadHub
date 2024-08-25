package com.iuh.fit.readhub.repositories;

import com.iuh.fit.readhub.models.Review;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReviewRepository extends JpaRepository<Review, Long> {
}