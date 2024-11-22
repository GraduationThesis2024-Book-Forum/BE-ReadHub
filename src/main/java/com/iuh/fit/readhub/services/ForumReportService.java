package com.iuh.fit.readhub.services;

import com.iuh.fit.readhub.constants.ReportStatus;
import com.iuh.fit.readhub.dto.ForumReportDTO;
import com.iuh.fit.readhub.dto.request.ReportActionRequest;
import com.iuh.fit.readhub.mapper.ForumReportMapper;
import com.iuh.fit.readhub.models.ForumReport;
import com.iuh.fit.readhub.models.User;
import com.iuh.fit.readhub.repositories.ForumReportRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ForumReportService {
    private final ForumReportRepository reportRepository;
    private final ForumService forumService;
    private final FCMService fcmService;
    private final ForumReportMapper reportMapper;

    @Transactional
    public ForumReport handleReportAction(Long reportId, ReportActionRequest request) {
        ForumReport report = reportRepository.findById(reportId)
                .orElseThrow(() -> new RuntimeException("Report not found"));

        User forumCreator = report.getForum().getCreator();

        switch (request.getAction()) {
            case DISMISS:
                report.setStatus(ReportStatus.DISMISSED);
                break;

            case WARN:
                report.setStatus(ReportStatus.WARNED);
                notifyUser(forumCreator, "Cảnh báo vi phạm", request.getReason());
                break;

            case BAN_1H:
                handleBan(report, forumCreator, request.getReason(), 1);
                break;

            case BAN_3H:
                handleBan(report, forumCreator, request.getReason(), 3);
                break;

            case BAN_24H:
                handleBan(report, forumCreator, request.getReason(), 24);
                break;

            case BAN_PERMANENT:
                handleBan(report, forumCreator, request.getReason(), null);
                break;
        }

        report.setResolvedAt(LocalDateTime.now());
        return reportRepository.save(report);
    }

    private void handleBan(ForumReport report, User user, String reason, Integer hours) {
        report.setStatus(ReportStatus.BANNED);
        forumService.banUser(user, reason, hours);

        String duration = hours != null ? hours + " giờ" : "vĩnh viễn";
        String message = String.format("Bạn đã bị cấm tạo diễn đàn trong %s. Lý do: %s",
                duration, reason);

        notifyUser(user, "Thông báo cấm tạo diễn đàn", message);
    }

    private void notifyUser(User user, String title, String message) {
        Map<String, String> data = Map.of(
                "type", "BAN_NOTIFICATION",
                "userId", user.getUserId().toString()
        );

        fcmService.sendNotification(user.getUserId(), title, message, data);
    }

    public List<ForumReportDTO> getAllReports() {
        return reportRepository.findAllOrderByReportedAtDesc()
                .stream()
                .map(reportMapper::toDTO)
                .collect(Collectors.toList());
    }

    // Lấy tất cả báo cáo đang chờ xử lý (PENDING)
    public List<ForumReportDTO> getPendingReports() {
        return reportRepository.findByStatus(ReportStatus.PENDING)
                .stream()
                .map(reportMapper::toDTO)
                .collect(Collectors.toList());
    }

    // Lấy báo cáo theo ID
    public ForumReportDTO getReportById(Long id) {
        ForumReport report = reportRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Report not found"));
        return reportMapper.toDTO(report);
    }

    public List<ForumReportDTO> getReportsByForumId(Long forumId) {
        return reportRepository.findByForum_DiscussionId(forumId)
                .stream()
                .map(reportMapper::toDTO)
                .collect(Collectors.toList());
    }

    public List<ForumReportDTO> getReportsByUserId(Long userId) {
        return reportRepository.findByReporter_UserId(userId)
                .stream()
                .map(reportMapper::toDTO)
                .collect(Collectors.toList());
    }
}