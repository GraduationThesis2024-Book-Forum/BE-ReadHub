package com.iuh.fit.readhub.services;

import com.iuh.fit.readhub.models.Note;
import com.iuh.fit.readhub.projections.NoteProjection;
import com.iuh.fit.readhub.repositories.NoteRepository;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@AllArgsConstructor
public class NoteService {

    @Autowired
    private NoteRepository noteRepository;

    public Note createNote(Note note) {
        return noteRepository.save(note);
    }

    public Optional<Note> getNoteById(Long id) {
        return noteRepository.findById(id);
    }

    public List<Note> getAllNotes() {
        return noteRepository.findAll();
    }

    public List<NoteProjection> getNotesByUserIdAndBookId(Long userId, Long bookId) {
        return noteRepository.findByUserIdAndBookId(userId, bookId);
    }

    public Note updateNote(Note note) {
        return noteRepository.save(note);
    }

    public void deleteNote(Long id) {
        noteRepository.deleteById(id);
    }
}