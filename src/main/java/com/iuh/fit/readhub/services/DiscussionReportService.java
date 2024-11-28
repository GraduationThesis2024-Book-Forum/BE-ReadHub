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