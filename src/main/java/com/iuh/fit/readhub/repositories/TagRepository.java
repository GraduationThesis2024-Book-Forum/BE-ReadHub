package com.iuh.fit.readhub.repositories;

import com.iuh.fit.readhub.models.Tag;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TagRepository extends JpaRepository<Tag, Long> {
}