package com.iuh.fit.readhub.repositories;

import com.iuh.fit.readhub.models.Book;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BookRepository extends JpaRepository<Book, Long> {
}