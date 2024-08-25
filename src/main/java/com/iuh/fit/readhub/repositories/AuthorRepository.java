package com.iuh.fit.readhub.repositories;

import com.iuh.fit.readhub.models.Author;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AuthorRepository extends JpaRepository<Author, Long> {
}