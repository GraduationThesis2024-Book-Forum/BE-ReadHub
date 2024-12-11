package com.iuh.fit.readhub.repositories;

import com.iuh.fit.readhub.constants.ReportStatus;
import com.iuh.fit.readhub.models.Discussion;
import com.iuh.fit.readhub.models.DiscussionReport;
import com.iuh.fit.readhub.models.User;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DiscussionReportRepository extends JpaRepository<DiscussionReport, Long> {
    List<DiscussionReport> findByDiscussion(Discussion discussion);
    List<DiscussionReport> findByStatus(ReportStatus status);

    // Thêm các method mới
    List<DiscussionReport> findByDiscussion_DiscussionId(Long discussionId);
    List<DiscussionReport> findByReporter_UserId(Long userId);

    // Sắp xếp theo thời gian
    @Query("SELECT fr FROM DiscussionReport fr ORDER BY fr.reportedAt DESC")
    List<DiscussionReport> findAllOrderByReportedAtDesc();

    // Thêm một số query hữu ích
    @Query("SELECT fr FROM DiscussionReport fr WHERE fr.discussion.discussionId = :discussionId AND fr.status = :status")
    List<DiscussionReport> findByDiscussionAndStatus(Long discussionId, ReportStatus status);

    @Query("SELECT COUNT(fr) FROM DiscussionReport fr WHERE fr.discussion.discussionId = :discussionId AND fr.status = :status")
    long countByDiscussionAndStatus(Long discussionId, ReportStatus status);

    @Modifying
    @Query("DELETE FROM DiscussionReport fs WHERE fs.discussion = ?1")
    void deleteByDiscussion(Discussion discussion);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT r FROM DiscussionReport r LEFT JOIN FETCH r.discussion LEFT JOIN FETCH r.reporter WHERE r.id = :id")
    Optional<DiscussionReport> findByIdWithLock(@Param("id") Long id);

    List<DiscussionReport> findByDiscussionAndStatus(Discussion discussion, ReportStatus status);

    long countByStatus(ReportStatus status);
}