package com.iuh.fit.readhub.repositories;

import com.iuh.fit.readhub.models.Discussion;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DiscussionRepository extends JpaRepository<Discussion, Long> {

    @Query("SELECT d FROM Discussion d ORDER BY d.createdAt DESC")
    List<Discussion> findAllByOrderByCreatedAtDesc();

    @Modifying
    @Query(value = "SELECT * FROM discussion WHERE discussion_id = :id FOR UPDATE", nativeQuery = true)
    @Transactional
    int lockDiscussion(@Param("id") Long id);
}