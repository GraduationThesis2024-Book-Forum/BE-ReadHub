package com.iuh.fit.readhub.repositories;

import com.iuh.fit.readhub.models.Note;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NoteRepository extends JpaRepository<Note, Long> {
}