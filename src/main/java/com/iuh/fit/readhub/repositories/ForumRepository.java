package com.iuh.fit.readhub.repositories;

import com.iuh.fit.readhub.models.Discussion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ForumRepository extends JpaRepository<Discussion, Long> {
    @Modifying
    @Query(value = "SELECT * FROM discussion WHERE discussion_id = :id FOR UPDATE", nativeQuery = true)
    Optional<Discussion> refreshAndLock(@Param("id") Long id);
}