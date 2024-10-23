package com.iuh.fit.readhub.repositories;

import com.iuh.fit.readhub.models.ReadingHistory;
import com.iuh.fit.readhub.models.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ReadingHistoryRepository extends JpaRepository<ReadingHistory, Long> {
    List<ReadingHistory> findByUser(User user);
}