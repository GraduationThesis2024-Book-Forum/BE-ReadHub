package com.iuh.fit.readhub.repositories;

import com.iuh.fit.readhub.models.Bookmark;
import com.iuh.fit.readhub.models.User;
import com.iuh.fit.readhub.projections.NoteProjection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface BookmarkRepository extends JpaRepository<Bookmark, Long> {
        @Query("SELECT b FROM Bookmark b WHERE b.user.userId = ?1 AND b.bookId = ?2")
        List<Bookmark> findByUserIdAndBookId(Long userId, Long bookId);
}