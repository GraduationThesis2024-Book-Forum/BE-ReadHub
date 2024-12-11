package com.iuh.fit.readhub.repositories;

import com.iuh.fit.readhub.models.Bookmark;
import com.iuh.fit.readhub.models.ReadingHistory;
import com.iuh.fit.readhub.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface ReadingHistoryRepository extends JpaRepository<ReadingHistory, Long> {
    @Query("SELECT rh FROM ReadingHistory rh " +
            "WHERE rh.user.userId = :userId AND rh.bookId = :bookId " +
            "ORDER BY rh.updatedAt DESC LIMIT 1")
    ReadingHistory findByUserIdAndBookId(Long userId, Long bookId);
    List<ReadingHistory> findByUser_UserIdOrderByUpdatedAtDesc(Long userId);
    List<ReadingHistory> findByUser_UserId(Long userId);

    @Query("SELECT COUNT(DISTINCT r.bookId) FROM ReadingHistory r " +
            "WHERE r.user.userId = :userId " +
            "AND r.createdAt >= :startDate " +  // Thêm = vào để bao gồm cả ngày bắt đầu
            "AND r.createdAt <= :endDate")     // Thêm = vào để bao gồm cả ngày kết thúc
    long countDistinctBooksByUserAndDateRange(
            @Param("userId") Long userId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate
    );
}