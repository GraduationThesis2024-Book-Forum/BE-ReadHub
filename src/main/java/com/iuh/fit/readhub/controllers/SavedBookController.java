package com.iuh.fit.readhub.controllers;

import com.iuh.fit.readhub.dto.ApiResponse;
import com.iuh.fit.readhub.services.SavedBookService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/saved-books")
public class SavedBookController {

    @Autowired
    private SavedBookService savedBookService;

    @PostMapping("/user/{userId}/book/{bookId}")
    @PreAuthorize("hasRole('ROLE_USER') or hasRole('ROLE_ADMIN')")
    public ResponseEntity<ApiResponse<?>> saveBook( @PathVariable Long bookId, @PathVariable Long userId) {
        savedBookService.saveBook(userId, bookId);
        return ResponseEntity.ok(ApiResponse.builder()
                .message("Lưu sách thành công")
                .status(200)
                .success(true)
                .build());
    }

    @DeleteMapping("/user/{userId}/book/{bookId}")
    @PreAuthorize("hasRole('ROLE_USER') or hasRole('ROLE_ADMIN')")
    public ResponseEntity<ApiResponse<?>> unsaveBook(@PathVariable Long userId, @PathVariable Long bookId) {
        savedBookService.unsaveBook(userId, bookId);
        return ResponseEntity.ok(ApiResponse.builder()
                .message("Xóa sách khỏi danh sách đã lưu thành công")
                .status(200)
                .success(true)
                .build());
    }

    @GetMapping("/user/{userId}")
    @PreAuthorize("hasRole('ROLE_USER') or hasRole('ROLE_ADMIN')")
    public ResponseEntity<ApiResponse<?>> getSavedBookIds(@PathVariable Long userId) {
        List<Long> bookIds = savedBookService.getSavedBookIds(userId);
        if (bookIds.isEmpty()) {
            return ResponseEntity.ok(ApiResponse.builder()
                    .data(bookIds)
                    .message("Không có sách nào được lưu")
                    .status(200)
                    .success(true)
                    .build());
        }
        return ResponseEntity.ok(ApiResponse.builder()
                .message("Lấy danh sách sách đã lưu thành công")
                .status(200)
                .data(bookIds)
                .success(true)
                .build());
    }

    @GetMapping("/user/{userId}/{bookId}/is-saved")
    @PreAuthorize("hasRole('ROLE_USER') or hasRole('ROLE_ADMIN')")
    public ResponseEntity<ApiResponse<?>> isBookSaved(@PathVariable Long userId, @PathVariable Long bookId) {
        boolean isSaved = savedBookService.isBookSaved(userId, bookId);
        return ResponseEntity.ok(ApiResponse.builder()
                .message("Kiểm tra trạng thái lưu sách thành công")
                .status(200)
                .data(isSaved)
                .success(true)
                .build());
    }
}