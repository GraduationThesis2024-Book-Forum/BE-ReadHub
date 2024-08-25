package com.iuh.fit.readhub.repositories;

import com.iuh.fit.readhub.models.UserBookProgress;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserBookProgressRepository extends JpaRepository<UserBookProgress, Long> {
}