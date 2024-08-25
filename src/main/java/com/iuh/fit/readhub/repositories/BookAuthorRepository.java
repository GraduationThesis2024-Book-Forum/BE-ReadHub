package com.iuh.fit.readhub.repositories;

import com.iuh.fit.readhub.models.BookAuthor;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BookAuthorRepository extends JpaRepository<BookAuthor, Long> {
}