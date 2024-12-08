package com.iuh.fit.readhub.controllers;

import com.iuh.fit.readhub.dto.BookListResponse;
import com.iuh.fit.readhub.dto.request.BookSearchCriteria;
import com.iuh.fit.readhub.models.Book;
import com.iuh.fit.readhub.services.BookService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("api/v1/book")
public class BookController {

    @Autowired
    private BookService bookService;

    @GetMapping("/{id}")
    public ResponseEntity<Optional<Book>> getBookById(@PathVariable Long id) {
        return ResponseEntity.ok(bookService.getBookById(id));
    }

    @GetMapping("/search")
    public ResponseEntity<BookListResponse> searchBooks(
            @RequestParam(required = false) String title,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "32") int size) {
        return ResponseEntity.ok(bookService.searchByTitle(title, page, size));
    }

    @PostMapping("/search/advanced")
    public ResponseEntity<BookListResponse> advancedSearch(
            @RequestBody BookSearchCriteria criteria,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "32") int size) {
        return ResponseEntity.ok(bookService.searchBooks(criteria, page, size));
    }

    @PostMapping("/batch")
    public ResponseEntity<BookListResponse> getBooksByIds(
            @RequestBody List<Long> ids,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "32") int size) {
        return ResponseEntity.ok(bookService.getBooksByIds(ids, page, size));
    }

    @GetMapping("/top-rated")
    public ResponseEntity<BookListResponse> getTopRatedBooks(){
        return ResponseEntity.ok(bookService.getTop20Book());
    }
}
