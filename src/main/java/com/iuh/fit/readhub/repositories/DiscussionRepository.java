package com.iuh.fit.readhub.repositories;

import com.iuh.fit.readhub.models.Discussion;
import jakarta.persistence.LockModeType;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DiscussionRepository extends JpaRepository<Discussion, Long> {

    @Query(value = "SELECT d FROM Discussion d WHERE d.discussionId = :id")
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<Discussion> findByIdWithLock(@Param("id") Long id);

    @Query("SELECT d FROM Discussion d ORDER BY d.createdAt DESC")
    List<Discussion> findAllByOrderByCreatedAtDesc();
}