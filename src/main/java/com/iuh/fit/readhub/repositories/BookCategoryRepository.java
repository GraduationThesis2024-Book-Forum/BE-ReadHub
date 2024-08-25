package com.iuh.fit.readhub.repositories;

import com.iuh.fit.readhub.models.BookCategory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BookCategoryRepository extends JpaRepository<BookCategory, Long> {
}