package com.iuh.fit.readhub.repositories;

import com.iuh.fit.readhub.models.Discussion;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DiscussionRepository extends JpaRepository<Discussion, Long> {
}