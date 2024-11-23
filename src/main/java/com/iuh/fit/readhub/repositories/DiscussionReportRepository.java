package com.iuh.fit.readhub.repositories;

import com.iuh.fit.readhub.constants.ReportStatus;
import com.iuh.fit.readhub.models.Discussion;
import com.iuh.fit.readhub.models.DiscussionReport;
import com.iuh.fit.readhub.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

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

    // Method hiện tại
    List<DiscussionReport> findByDiscussionOrderByReportedAtDesc(Discussion discussion);
    List<DiscussionReport> findByReporterOrderByReportedAtDesc(User reporter);

    // Thêm một số query hữu ích
    @Query("SELECT fr FROM DiscussionReport fr WHERE fr.discussion.discussionId = :discussionId AND fr.status = :status")
    List<DiscussionReport> findByDiscussionAndStatus(Long discussionId, ReportStatus status);

    @Query("SELECT COUNT(fr) FROM DiscussionReport fr WHERE fr.discussion.discussionId = :discussionId AND fr.status = :status")
    long countByDiscussionAndStatus(Long discussionId, ReportStatus status);

    @Modifying
    @Query("DELETE FROM DiscussionReport fs WHERE fs.discussion = ?1")
    void deleteByDiscussion(Discussion discussion);

}