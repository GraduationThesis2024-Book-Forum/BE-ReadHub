package com.iuh.fit.readhub.repositories;

import com.iuh.fit.readhub.models.Bookmark;
import com.iuh.fit.readhub.models.User;
import com.iuh.fit.readhub.projections.NoteProjection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface BookmarkRepository extends JpaRepository<Bookmark, Long> {
        @Query(value = "SELECT *   " +
            "FROM bookmark n WHERE n.user_id = ?1 AND n.book_id = ?2", nativeQuery = true)
        Bookmark findByUserIdAndBookId(Long userId, Long bookId);
}