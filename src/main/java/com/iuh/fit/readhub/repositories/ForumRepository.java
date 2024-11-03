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
    @Query("refresh refresh entity Discussion d where d.id = :id")
    void refresh(@Param("id") Discussion discussion);
}