package com.iuh.fit.readhub.services;

import com.iuh.fit.readhub.constants.ReportStatus;
import com.iuh.fit.readhub.dto.DiscussionReportDTO;
import com.iuh.fit.readhub.dto.request.ReportActionRequest;
import com.iuh.fit.readhub.mapper.DiscussionReportMapper;
import com.iuh.fit.readhub.models.DiscussionReport;
import com.iuh.fit.readhub.models.User;
import com.iuh.fit.readhub.repositories.DiscussionReportRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DiscussionReportService {
    private final DiscussionReportRepository reportRepository;
    private final DiscussionService discussionService;
    private final FCMService fcmService;
    private final DiscussionReportMapper reportMapper;

    @Transactional
    public DiscussionReport handleReportAction(Long reportId, ReportActionRequest request) {
        DiscussionReport report = reportRepository.findById(reportId)
                .orElseThrow(() -> new RuntimeException("Report not found"));

        User discussionCreator = report.getDiscussion().getCreator();

        switch (request.getAction()) {
            case DISMISS:
                report.setStatus(ReportStatus.DISMISSED);
                break;

            case WARN:
                report.setStatus(ReportStatus.WARNED);
                notifyUser(discussionCreator, "Cảnh báo vi phạm", request.getReason());
                break;

            case BAN_1H:
                handleBan(report, discussionCreator, request.getReason(), 1);
                break;

            case BAN_3H:
                handleBan(report, discussionCreator, request.getReason(), 3);
                break;

            case BAN_24H:
                handleBan(report, discussionCreator, request.getReason(), 24);
                break;

            case BAN_PERMANENT:
                handleBan(report, discussionCreator, request.getReason(), null);
                break;
        }

        report.setResolvedAt(LocalDateTime.now());
        return reportRepository.save(report);
    }

    private void handleBan(DiscussionReport report, User user, String reason, Integer hours) {
        report.setStatus(ReportStatus.BANNED);
        discussionService.banUser(user, reason, hours);

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

    public List<DiscussionReportDTO> getAllReports() {
        return reportRepository.findAllOrderByReportedAtDesc()
                .stream()
                .map(reportMapper::toDTO)
                .collect(Collectors.toList());
    }

    // Lấy tất cả báo cáo đang chờ xử lý (PENDING)
    public List<DiscussionReportDTO> getPendingReports() {
        return reportRepository.findByStatus(ReportStatus.PENDING)
                .stream()
                .map(reportMapper::toDTO)
                .collect(Collectors.toList());
    }

    // Lấy báo cáo theo ID
    public DiscussionReportDTO getReportById(Long id) {
        DiscussionReport report = reportRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Report not found"));
        return reportMapper.toDTO(report);
    }

    public List<DiscussionReportDTO> getReportsByForumId(Long forumId) {
        return reportRepository.findByDiscussion_DiscussionId(forumId)
                .stream()
                .map(reportMapper::toDTO)
                .collect(Collectors.toList());
    }

    public List<DiscussionReportDTO> getReportsByUserId(Long userId) {
        return reportRepository.findByReporter_UserId(userId)
                .stream()
                .map(reportMapper::toDTO)
                .collect(Collectors.toList());
    }
}