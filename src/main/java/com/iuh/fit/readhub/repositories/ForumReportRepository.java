package com.iuh.fit.readhub.repositories;

import com.iuh.fit.readhub.constants.ReportStatus;
import com.iuh.fit.readhub.models.Discussion;
import com.iuh.fit.readhub.models.ForumReport;
import com.iuh.fit.readhub.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ForumReportRepository extends JpaRepository<ForumReport, Long> {
    List<ForumReport> findByForum(Discussion forum);
    List<ForumReport> findByStatus(ReportStatus status);

    // Thêm các method mới
    List<ForumReport> findByForum_DiscussionId(Long forumId);
    List<ForumReport> findByReporter_UserId(Long userId);

    // Sắp xếp theo thời gian
    List<ForumReport> findByForum_DiscussionIdOrderByReportedAtDesc(Long forumId);
    List<ForumReport> findByReporter_UserIdOrderByReportedAtDesc(Long userId);

    // Method hiện tại
    List<ForumReport> findByForumOrderByReportedAtDesc(Discussion forum);
    List<ForumReport> findByReporterOrderByReportedAtDesc(User reporter);
    boolean existsByForumAndReporterAndStatus(Discussion forum, User reporter, ReportStatus status);

    // Thêm một số query hữu ích
    @Query("SELECT fr FROM ForumReport fr WHERE fr.forum.discussionId = :forumId AND fr.status = :status")
    List<ForumReport> findByForumAndStatus(Long forumId, ReportStatus status);

    @Query("SELECT COUNT(fr) FROM ForumReport fr WHERE fr.forum.discussionId = :forumId AND fr.status = :status")
    long countByForumAndStatus(Long forumId, ReportStatus status);
}