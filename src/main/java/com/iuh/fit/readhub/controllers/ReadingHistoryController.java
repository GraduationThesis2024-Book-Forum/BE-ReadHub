package com.iuh.fit.readhub.controllers;

import com.iuh.fit.readhub.dto.ApiResponse;
import com.iuh.fit.readhub.dto.request.ReadingHistoryRequest;
import com.iuh.fit.readhub.repositories.UserRepository;
import com.iuh.fit.readhub.services.ReadingHistoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/api/v1/reading-history")
public class ReadingHistoryController {
    @Autowired
    private ReadingHistoryService readingHistoryService;

    @Autowired
    private UserRepository userRepository;

    @PostMapping
    @PreAuthorize("hasRole('ROLE_USER') or hasRole('ROLE_ADMIN')")
    public ResponseEntity<ApiResponse<?>> createReadingHistory(@RequestBody ReadingHistoryRequest readingHistoryRequest) {
        readingHistoryService.createReadingHistory(readingHistoryRequest);
        return ResponseEntity.ok(ApiResponse.builder()
                .message("Tạo lịch sử đọc sách thành công")
                .status(200)
                .success(true)
                .build());
    }

    @GetMapping("user/{userId}")
    @PreAuthorize("hasRole('ROLE_USER') or hasRole('ROLE_ADMIN')")
    public ResponseEntity<ApiResponse<?>> getReadingHistoryByUserId(@PathVariable Long userId) {
        return ResponseEntity.ok(ApiResponse.builder()
                .message("Lấy lịch sử đọc sách thành công")
                .status(200)
                .data(readingHistoryService.getReadingHistoryIdsByUserId(userId))
                .success(true)
                .build());
    }

}
