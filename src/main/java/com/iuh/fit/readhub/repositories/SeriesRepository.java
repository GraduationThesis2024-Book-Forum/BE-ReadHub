package com.iuh.fit.readhub.repositories;

import com.iuh.fit.readhub.models.Series;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SeriesRepository extends JpaRepository<Series, Long> {
}