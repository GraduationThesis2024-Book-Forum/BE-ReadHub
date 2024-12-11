package com.iuh.fit.readhub.repositories;

import com.iuh.fit.readhub.constants.ReportStatus;
import com.iuh.fit.readhub.models.Comment;
import com.iuh.fit.readhub.models.CommentReport;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
public interface CommentReportRepository extends JpaRepository<CommentReport, Long> {
    List<CommentReport> findByStatus(ReportStatus status);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT r FROM CommentReport r WHERE r.id = :id")
    Optional<CommentReport> findByIdWithLock(@Param("id") Long id);

    @Modifying
    @Transactional
    void deleteByComment(Comment comment);

    List<CommentReport> findByCommentAndStatus(Comment comment, ReportStatus status);
}