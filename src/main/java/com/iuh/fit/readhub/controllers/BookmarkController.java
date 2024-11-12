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

@Controller
@RequestMapping("api/v1/bookmark")
public class BookmarkController {
    @Autowired
    private BookmarkService bookmarkService;

    @PostMapping
    @PreAuthorize("hasRole('ROLE_USER') or hasRole('ROLE_ADMIN')")
    public ResponseEntity<ApiResponse<?>> createBookmark(@RequestBody BookmarkRequest bookmarkRequest) {
        bookmarkService.createBookmark(bookmarkRequest);
        return ResponseEntity.ok(ApiResponse.builder()
                .message("Tạo bookmark thành công")
                .status(200)
                .success(true)
                .build());
    }

    @PutMapping
    @PreAuthorize("hasRole('ROLE_USER') or hasRole('ROLE_ADMIN')")
    public ResponseEntity<ApiResponse<?>> updateBookmark(@RequestBody BookmarkRequest bookmarkRequest) {
        bookmarkService.updateBookmark(bookmarkRequest);
        return ResponseEntity.ok(ApiResponse.builder()
                .message("Cập nhật bookmark thành công")
                .status(200)
                .success(true)
                .build());
    }

    @GetMapping("/user/{userId}/book/{bookId}")
    @PreAuthorize("hasRole('ROLE_USER') or hasRole('ROLE_ADMIN')")
    public ResponseEntity<ApiResponse<?>> getBookmarkByUserIdAndBookId(@PathVariable Long userId, @PathVariable Long bookId) {
        Bookmark bookmark = bookmarkService.getBookmarkByUserIdAndBookId(userId, bookId);
        bookmark.setUser(null);
        return ResponseEntity.ok(ApiResponse.builder()
                .message("Lấy bookmark thành công")
                .status(200)
                .data(bookmark)
                .success(true)
                .build());
    }
}
