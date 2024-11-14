package com.iuh.fit.readhub.repositories;

import com.iuh.fit.readhub.models.Discussion;
import com.iuh.fit.readhub.models.ForumReport;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ForumReportRepository extends JpaRepository<ForumReport, Long> {
    List<ForumReport> findByForum(Discussion forum);
}