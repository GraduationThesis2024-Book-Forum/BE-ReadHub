package com.iuh.fit.readhub.controllers;

import com.iuh.fit.readhub.dto.ApiResponse;
import com.iuh.fit.readhub.dto.UserResponse;
import com.iuh.fit.readhub.dto.request.NoteRequest;
import com.iuh.fit.readhub.models.Note;
import com.iuh.fit.readhub.projections.NoteProjection;
import com.iuh.fit.readhub.repositories.UserRepository;
import com.iuh.fit.readhub.services.NoteService;
import com.iuh.fit.readhub.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;


@RestController
@RequestMapping("api/v1/note")
public class NoteController {

    @Autowired
    private NoteService noteService;

    @Autowired
    private UserRepository userRepository;

    @GetMapping
    @PreAuthorize("hasRole('ROLE_ADMIṆ') || hasRole('ROLE_USER')")
    public ResponseEntity<ApiResponse<?>> getAllNote() {
        List<Note> notes = noteService.getAllNotes();
        if (notes.isEmpty()) {
            return ResponseEntity.ok(ApiResponse.builder()
                    .data(notes)
                    .message("Không có note nào")
                    .status(200)
                    .success(true)
                    .build());
        }
        return ResponseEntity.ok(ApiResponse.builder()
                .message("Lấy danh sách note thành công")
                .status(200)
                .data(noteService.getAllNotes())
                .success(true)
                .build());
    }
//    getById
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ROLE_ADMIṆ') || hasRole('ROLE_USER')")
    public ResponseEntity<ApiResponse<?>> getNoteById(@PathVariable Long id) {
        Note note = noteService.getNoteById(id).get();
        return ResponseEntity.ok(ApiResponse.builder()
                .message("Lấy note thành công")
                .status(200)
                .data(note.toString())
                .success(true)
                .build());
    }

    @PostMapping
    @PreAuthorize("hasRole('ROLE_ADMIṆ') || hasRole('ROLE_USER')")
    public ResponseEntity<ApiResponse<?>> createNote(@RequestBody NoteRequest note) {
        Note newNote =new Note();
        newNote.setUser(userRepository.findById(note.getUserId()).get());
        newNote.setBookId(note.getBookId());
        newNote.setContent(note.getContent());
        newNote.setSelectedText(note.getSelectedText());
        newNote.setCfiRange(note.getCfiRange());
        newNote.setColor(note.getColor());
        noteService.createNote(newNote);
        return ResponseEntity.ok(ApiResponse.builder()
                .message("Tạo note thành công")
                .status(200)
                .data(newNote.toString())
                .success(true)
                .build());
    }

    @GetMapping("/user/{userId}/book/{bookId}")
    @PreAuthorize("hasRole('ROLE_ADMIṆ') || hasRole('ROLE_USER')")
    public ResponseEntity<ApiResponse<?>> getNoteByUserIdAndBookId(@PathVariable Long userId, @PathVariable Long bookId) {
        List<NoteProjection> notes = noteService.getNotesByUserIdAndBookId(userId, bookId);
        if (notes.isEmpty()) {
            return ResponseEntity.ok(ApiResponse.builder()
                    .data(notes)
                    .message("Không có note nào")
                    .status(200)
                    .success(true)
                    .build());
        }
        return ResponseEntity.ok(ApiResponse.builder()
                .message("Lấy danh sách note thành công")
                .status(200)
                .data(notes)
                .success(true)
                .build());
    }

    @PutMapping
    @PreAuthorize("hasRole('ROLE_ADMIṆ') || hasRole('ROLE_USER')")
    public ResponseEntity<ApiResponse<?>> updateNote(@RequestBody NoteRequest note) {
        Note newNote = noteService.getNoteById(note.getNoteId()).get();
        newNote.setContent(note.getContent() != null ? note.getContent() : newNote.getContent());
        newNote.setColor(note.getColor() != null ? note.getColor() : newNote.getColor());
        noteService.updateNote(newNote);
        return ResponseEntity.ok(ApiResponse.builder()
                .message("Cập nhật note thành công")
                .status(200)
                .data(newNote.toString())
                .success(true)
                .build());
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ROLE_ADMIṆ') || hasRole('ROLE_USER')")
    public ResponseEntity<ApiResponse<?>> deleteNoteById(@PathVariable Long id) {
        noteService.deleteNote(id);
        return ResponseEntity.ok(ApiResponse.builder()
                .message("Xóa note thành công")
                .status(200)
                .success(true)
                .build());
    }



}
