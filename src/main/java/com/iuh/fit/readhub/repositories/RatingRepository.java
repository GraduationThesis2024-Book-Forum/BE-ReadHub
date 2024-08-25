package com.iuh.fit.readhub.repositories;

import com.iuh.fit.readhub.models.Rating;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RatingRepository extends JpaRepository<Rating, Long> {
}