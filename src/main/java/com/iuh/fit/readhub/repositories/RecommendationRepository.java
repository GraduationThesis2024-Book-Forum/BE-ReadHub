package com.iuh.fit.readhub.repositories;

import com.iuh.fit.readhub.models.Recommendation;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RecommendationRepository extends JpaRepository<Recommendation, Long> {
}