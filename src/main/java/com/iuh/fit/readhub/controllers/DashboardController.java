package com.iuh.fit.readhub.controllers;

import com.iuh.fit.readhub.constants.ReportStatus;
import com.iuh.fit.readhub.dto.ApiResponse;
import com.iuh.fit.readhub.repositories.DiscussionReportRepository;
import com.iuh.fit.readhub.repositories.DiscussionRepository;
import com.iuh.fit.readhub.repositories.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/admin/dashboard")
@PreAuthorize("hasRole('ROLE_ADMIN')")
public class DashboardController {
    private final UserRepository userRepository;
    private final DiscussionRepository discussionRepository;
    private final DiscussionReportRepository discussionReportRepository;
    public DashboardController(UserRepository userRepository, DiscussionRepository discussionRepository, DiscussionReportRepository discussionReportRepository) {
        this.userRepository = userRepository;
        this.discussionRepository = discussionRepository;
        this.discussionReportRepository = discussionReportRepository;
    }
    @GetMapping("/stats")
    public ResponseEntity<ApiResponse<?>> getDashboardStats() {
        try {
            LocalDateTime startOfMonth = LocalDateTime.now()
                    .withDayOfMonth(1)
                    .withHour(0)
                    .withMinute(0)
                    .withSecond(0);
            long newUsers = userRepository.countByCreatedAtAfter(startOfMonth);
            long totalForums = discussionRepository.count();
            long reportedForums = discussionReportRepository.countByStatus(ReportStatus.PENDING);
            Map<String, Object> stats = new HashMap<>();
            stats.put("newUsers", newUsers);
            stats.put("totalForums", totalForums);
            stats.put("reportedForums", reportedForums);
            return ResponseEntity.ok(ApiResponse.builder()
                    .data(stats)
                    .message("Lấy thống kê thành công")
                    .status(200)
                    .success(true)
                    .build());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.builder()
                    .message("Lỗi khi lấy thống kê: " + e.getMessage())
                    .status(400)
                    .success(false)
                    .build());
        }
    }
}