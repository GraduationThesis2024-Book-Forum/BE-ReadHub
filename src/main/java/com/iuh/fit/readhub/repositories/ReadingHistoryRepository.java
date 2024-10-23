package com.iuh.fit.readhub.repositories;

import com.iuh.fit.readhub.models.Bookmark;
import com.iuh.fit.readhub.models.ReadingHistory;
import com.iuh.fit.readhub.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface ReadingHistoryRepository extends JpaRepository<ReadingHistory, Long> {
    @Query(value = "SELECT *   " +
            "FROM reading_history n WHERE n.user_id = ?1 AND n.book_id = ?2", nativeQuery = true)
    ReadingHistory findByUserIdAndBookId(Long userId, Long bookId);

    List<ReadingHistory> findByUser(User user);
}