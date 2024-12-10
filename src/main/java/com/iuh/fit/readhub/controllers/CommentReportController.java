package com.iuh.fit.readhub.controllers;

import com.iuh.fit.readhub.dto.ApiResponse;
import com.iuh.fit.readhub.dto.CommentReportDTO;
import com.iuh.fit.readhub.dto.request.CommentReportActionRequest;
import com.iuh.fit.readhub.dto.request.CommentReportRequest;
import com.iuh.fit.readhub.models.User;
import com.iuh.fit.readhub.services.CommentReportService;
import com.iuh.fit.readhub.services.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/comment-reports")
@RequiredArgsConstructor
public class CommentReportController {
    private final CommentReportService commentReportService;
    private final UserService userService;

    @PostMapping("/{commentId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<?>> reportComment(
            @PathVariable Long commentId,
            @RequestBody CommentReportRequest request,
            Authentication authentication) {
        try {
            User reporter = userService.getCurrentUser(authentication);
            commentReportService.reportComment(commentId, reporter, request.getReason(), request.getAdditionalInfo());
            return ResponseEntity.ok(ApiResponse.builder()
                    .message("Comment reported successfully")
                    .status(200)
                    .success(true)
                    .build());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.builder()
                    .message("Error reporting comment: " + e.getMessage())
                    .status(400)
                    .success(false)
                    .build());
        }
    }

    @GetMapping("/pending")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<ApiResponse<?>> getPendingReports() {
        try {
            List<CommentReportDTO> reports = commentReportService.getPendingReports();
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

    @PostMapping("/reports/{reportId}/action")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<ApiResponse<?>> handleReportAction(
            @PathVariable Long reportId,
            @RequestBody CommentReportActionRequest request,
            Authentication authentication) {
        try {
            commentReportService.handleReportAction(reportId, request);
            return ResponseEntity.ok(ApiResponse.builder()
                    .message("Report action applied successfully")
                    .status(200)
                    .success(true)
                    .build());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.builder()
                    .message("Error applying report action: " + e.getMessage())
                    .status(400)
                    .success(false)
                    .build());
        }
    }
}