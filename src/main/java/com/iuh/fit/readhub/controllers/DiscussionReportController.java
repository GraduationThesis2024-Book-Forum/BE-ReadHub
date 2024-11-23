package com.iuh.fit.readhub.controllers;

import com.iuh.fit.readhub.dto.ApiResponse;
import com.iuh.fit.readhub.dto.DiscussionReportDTO;
import com.iuh.fit.readhub.services.DiscussionReportService;
import com.iuh.fit.readhub.services.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/forum-reports")
@RequiredArgsConstructor
public class DiscussionReportController {

    private final DiscussionReportService discussionReportService;
    private final UserService userService;

    @GetMapping
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<ApiResponse<?>> getAllReports() {
        try {
            List<DiscussionReportDTO> reports = discussionReportService.getAllReports();
            return ResponseEntity.ok(ApiResponse.builder()
                    .message("Reports fetched successfully")
                    .status(200)
                    .data(reports)
                    .success(true)
                    .build());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.builder()
                    .message("Error fetching reports: " + e.getMessage())
                    .status(400)
                    .success(false)
                    .build());
        }
    }

    @GetMapping("/pending")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<ApiResponse<?>> getPendingReports() {
        try {
            List<DiscussionReportDTO> reports = discussionReportService.getPendingReports();
            return ResponseEntity.ok(ApiResponse.builder()
                    .message("Pending reports fetched successfully")
                    .status(200)
                    .data(reports)
                    .success(true)
                    .build());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.builder()
                    .message("Error fetching pending reports: " + e.getMessage())
                    .status(400)
                    .success(false)
                    .build());
        }
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<ApiResponse<?>> getReportById(@PathVariable Long id) {
        try {
            DiscussionReportDTO report = discussionReportService.getReportById(id);
            return ResponseEntity.ok(ApiResponse.builder()
                    .message("Report fetched successfully")
                    .status(200)
                    .data(report)
                    .success(true)
                    .build());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.builder()
                    .message("Error fetching report: " + e.getMessage())
                    .status(400)
                    .success(false)
                    .build());
        }
    }

    @GetMapping("/forum/{forumId}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<ApiResponse<?>> getReportsByForum(@PathVariable Long forumId) {
        try {
            List<DiscussionReportDTO> reports = discussionReportService.getReportsByForumId(forumId);
            return ResponseEntity.ok(ApiResponse.builder()
                    .message("Forum reports fetched successfully")
                    .status(200)
                    .data(reports)
                    .success(true)
                    .build());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.builder()
                    .message("Error fetching forum reports: " + e.getMessage())
                    .status(400)
                    .success(false)
                    .build());
        }
    }

    @GetMapping("/user/{userId}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<ApiResponse<?>> getReportsByUser(@PathVariable Long userId) {
        try {
            List<DiscussionReportDTO> reports = discussionReportService.getReportsByUserId(userId);
            return ResponseEntity.ok(ApiResponse.builder()
                    .message("User reports fetched successfully")
                    .status(200)
                    .data(reports)
                    .success(true)
                    .build());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.builder()
                    .message("Error fetching user reports: " + e.getMessage())
                    .status(400)
                    .success(false)
                    .build());
        }
    }
}