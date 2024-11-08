package com.iuh.fit.readhub;

import com.iuh.fit.readhub.models.SavedBook;
import com.iuh.fit.readhub.models.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SavedBookRepository extends JpaRepository<SavedBook, Long> {
    List<SavedBook> findByUser_UserIdOrderBySavedAtDesc(Long userId);
    Optional<SavedBook> findByUser_UserIdAndBookId(Long userId, Long bookId);
    boolean existsByUser_UserIdAndBookId(Long userId, Long bookId);
    void deleteByUser_UserIdAndBookId(Long userId, Long bookId);
}