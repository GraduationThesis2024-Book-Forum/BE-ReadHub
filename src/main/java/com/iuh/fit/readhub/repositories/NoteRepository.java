package com.iuh.fit.readhub.repositories;

import com.iuh.fit.readhub.models.Note;
import com.iuh.fit.readhub.projections.NoteProjection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface NoteRepository extends JpaRepository<Note, Long> {

    @Query(value = "SELECT n.note_id as noteId, n.content, n.selected_text as selectedText, n.user_id as userId, n.book_id as bookId, n.color, n.cfi_range as cfiRange, " +
            "DATE_FORMAT(n.created_at, '%Y-%m-%d %H:%i:%s') as createdAt, " +
            "DATE_FORMAT(n.updated_at, '%Y-%m-%d %H:%i:%s') as updatedAt " +
            "FROM note n WHERE n.user_id = ?1 AND n.book_id = ?2", nativeQuery = true)
    List<NoteProjection> findByUserIdAndBookId(Long userId, Long bookId);
}