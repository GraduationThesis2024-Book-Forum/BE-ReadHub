package com.iuh.fit.readhub.repositories;

import com.iuh.fit.readhub.models.ReadingHistory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReadingHistoryRepository extends JpaRepository<ReadingHistory, Long> {
}