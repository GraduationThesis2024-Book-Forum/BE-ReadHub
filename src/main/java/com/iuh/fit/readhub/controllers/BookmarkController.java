package com.iuh.fit.readhub.controllers;

import com.iuh.fit.readhub.dto.ApiResponse;
import com.iuh.fit.readhub.dto.request.BookmarkRequest;
import com.iuh.fit.readhub.models.Bookmark;
import com.iuh.fit.readhub.services.BookmarkService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/api/v1/bookmark")
public class BookmarkController {
    @Autowired
    private BookmarkService bookmarkService;

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<?>> createBookmark(@RequestBody BookmarkRequest bookmarkRequest) {
        bookmarkService.createBookmark(bookmarkRequest);
        return ResponseEntity.ok(ApiResponse.builder()
                .message("Tạo bookmark thành công")
                .status(200)
                .success(true)
                .build());
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<?>> removeBookmark(@PathVariable Long id) {
        bookmarkService.removeBookmark(id);
        return ResponseEntity.ok(ApiResponse.builder()
                .message("Xóa bookmark thành công")
                .status(200)
                .success(true)
                .build());
    }

    @GetMapping("/user/{userId}/book/{bookId}")
    @PreAuthorize("hasRole('ROLE_USER') or hasRole('ROLE_ADMIN')")
    public ResponseEntity<ApiResponse<?>> getBookmarksByUserIdAndBookId(@PathVariable Long userId, @PathVariable Long bookId) {
        List<Bookmark> bookmarks = bookmarkService.getBookmarksByUserIdAndBookId(userId, bookId);
        if (bookmarks == null) {
            return ResponseEntity.ok(ApiResponse.builder()
                    .message("Chưa có bookmark")
                    .status(200)
                    .data(null)
                    .success(true)
                    .build());
        }
        bookmarks.forEach(bookmark -> bookmark.setUser(null));
        return ResponseEntity.ok(ApiResponse.builder()
                .message("Lấy bookmark thành công")
                .status(200)
                .data(bookmarks)
                .success(true)
                .build());
    }
}
