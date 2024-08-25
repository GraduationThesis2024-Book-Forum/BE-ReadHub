package com.iuh.fit.readhub.repositories;

import com.iuh.fit.readhub.models.BookTag;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BookTagRepository extends JpaRepository<BookTag, Long> {
}