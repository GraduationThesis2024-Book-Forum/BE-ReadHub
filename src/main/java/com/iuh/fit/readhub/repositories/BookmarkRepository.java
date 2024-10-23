package com.iuh.fit.readhub.repositories;

import com.iuh.fit.readhub.models.Bookmark;
import com.iuh.fit.readhub.models.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BookmarkRepository extends JpaRepository<Bookmark, Long> {
    Bookmark findByUserAndBookId(User user, Long bookId);
}