package com.iuh.fit.readhub.services;

import com.iuh.fit.readhub.constants.CommentReportReason;
import com.iuh.fit.readhub.constants.NotificationType;
import com.iuh.fit.readhub.constants.ReportAction;
import com.iuh.fit.readhub.constants.ReportStatus;
import com.iuh.fit.readhub.dto.CommentReportDTO;
import com.iuh.fit.readhub.dto.request.CommentReportActionRequest;
import com.iuh.fit.readhub.mapper.UserMapper;
import com.iuh.fit.readhub.models.Comment;
import com.iuh.fit.readhub.models.CommentReport;
import com.iuh.fit.readhub.models.User;
import com.iuh.fit.readhub.repositories.CommentReportRepository;
import com.iuh.fit.readhub.repositories.CommentRepository;
import com.iuh.fit.readhub.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class CommentReportService {
    private final CommentReportRepository commentReportRepository;
    private final CommentRepository commentRepository;
    private final UserService userService;
    private final UserRepository userRepository;
    private final FCMService fcmService;
    private final UserMapper userMapper;

    public void reportComment(Long commentId, User reporter, CommentReportReason reason, String additionalInfo) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new RuntimeException("Comment not found"));

        if (comment.getUser().getUserId().equals(reporter.getUserId())) {
            throw new RuntimeException("You cannot report your own comment");
        }

        CommentReport report = CommentReport.builder()
                .comment(comment)
                .reporter(reporter)
                .reason(reason)
                .additionalInfo(additionalInfo)
                .reportedAt(LocalDateTime.now())
                .status(ReportStatus.PENDING)
                .build();

        commentReportRepository.save(report);
        List<User> admins = userService.getAllAdmins();
        for (User admin : admins) {
            Map<String, String> notificationData = Map.of(
                    "type", NotificationType.COMMENT_REPORT.name(),
                    "commentId", commentId.toString(),
                    "reportId", report.getId().toString(),
                    "reporterId", reporter.getUserId().toString(),
                    "reason", reason.name()
            );

            String notificationMessage = String.format(
                    "Comment by %s has been reported by %s for %s",
                    comment.getUser().getFullName(),
                    reporter.getFullName(),
                    reason.getDescription()
            );

            fcmService.sendNotification(
                    admin.getUserId(),
                    "New Comment Report",
                    notificationMessage,
                    notificationData
            );
        }
    }

    @Transactional
    public void handleReportAction(Long reportId, CommentReportActionRequest request) {
        CommentReport mainReport = commentReportRepository.findByIdWithLock(reportId)
                .orElseThrow(() -> new RuntimeException("Report not found"));
        if (mainReport.getStatus() != ReportStatus.PENDING) {
            throw new RuntimeException("Report has already been handled");
        }
        Comment comment = mainReport.getComment();
        User commentAuthor = comment.getUser();
        List<CommentReport> relatedReports = commentReportRepository
                .findByCommentAndStatus(comment, ReportStatus.PENDING);
        for (CommentReport report : relatedReports) {
            updateReportStatus(report, request.getAction());
            report.setResolvedAt(LocalDateTime.now());
            report.setResolution(request.getReason());
            commentReportRepository.save(report);
        }
        switch (request.getAction()) {
            case DISMISS:
                break;
            case BAN_24H:
                handleTemporaryBan(commentAuthor, 24, request.getReason(), request.getBanTypes());
                break;
            case BAN_PERMANENT:
                handlePermanentBan(commentAuthor, request.getReason(), request.getBanTypes());
                break;
        }
        if (request.getBanTypes() != null && request.getBanTypes().isDeleteComment()) {
            commentReportRepository.deleteByComment(comment);
            commentRepository.delete(comment);
        }
        for (CommentReport report : relatedReports) {
            notifyReporter(report.getReporter(), request.getAction(), request.getReason());
        }
        notifyCommentAuthor(commentAuthor, request.getAction(), request.getReason(),
                request.getBanTypes() != null && request.getBanTypes().isDeleteComment());
    }

    private void updateReportStatus(CommentReport report, ReportAction action) {
        switch (action) {
            case DISMISS:
                report.setStatus(ReportStatus.DISMISSED);
                break;
            case BAN_24H:
            case BAN_PERMANENT:
                report.setStatus(ReportStatus.BANNED);
                break;
            default:
                throw new RuntimeException("Invalid action");
        }
        report.setResolvedAt(LocalDateTime.now());
    }

    private void notifyReporter(User reporter, ReportAction action, String reason) {
        try {
            String title;
            String message;
            Map<String, String> data = new HashMap<>();
            data.put("type", NotificationType.REPORT_ACTION.name());
            data.put("action", action.name());

            switch (action) {
                case DISMISS:
                    title = "Report Update: Dismissed";
                    message = "Your report has been reviewed and dismissed by moderators.";
                    break;
                case BAN_24H:
                    title = "Report Update: Action Taken";
                    message = "Your report has been reviewed. The user has been temporarily banned. Reason: " + reason;
                    break;
                case BAN_PERMANENT:
                    title = "Report Update: Action Taken";
                    message = "Your report has been reviewed. The user has been permanently banned. Reason: " + reason;
                    break;
                default:
                    title = "Report Update";
                    message = "Your report has been processed.";
            }
            fcmService.sendNotification(
                    reporter.getUserId(),
                    title,
                    message,
                    data
            );

        } catch (Exception e) {
            log.error("Failed to send notification to reporter: {}", e.getMessage());
        }
    }

    private void notifyCommentAuthor(User author, ReportAction action, String reason, boolean isCommentDeleted) {
        try {
            String title = "Comment Report Action";
            String message;
            Map<String, String> data = new HashMap<>();
            data.put("type", NotificationType.COMMENT_REPORT.name());
            data.put("action", action.name());

            switch (action) {
                case DISMISS:
                    message = "A report against your comment was reviewed and dismissed.";
                    break;
                case BAN_24H:
                    message = String.format(
                            "You have been temporarily banned for 24 hours. Reason: %s%s",
                            reason,
                            isCommentDeleted ? " Your comment has been deleted." : ""
                    );
                    break;
                case BAN_PERMANENT:
                    message = String.format(
                            "You have been permanently banned. Reason: %s%s",
                            reason,
                            isCommentDeleted ? " Your comment has been deleted." : ""
                    );
                    break;
                default:
                    message = "Action has been taken on your comment.";
            }

            fcmService.sendNotification(
                    author.getUserId(),
                    title,
                    message,
                    data
            );

        } catch (Exception e) {
            log.error("Failed to send notification to comment author: {}", e.getMessage());
        }
    }

    public List<CommentReportDTO> getPendingReports() {
        return commentReportRepository.findByStatus(ReportStatus.PENDING)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    private CommentReportDTO convertToDTO(CommentReport report) {
        return CommentReportDTO.builder()
                .id(report.getId())
                .commentId(report.getComment().getCommentId())
                .commentContent(report.getComment().getContent())
                .reporter(userMapper.toDTO(report.getReporter()))
                .commentAuthor(userMapper.toDTO(report.getComment().getUser()))
                .reason(report.getReason())
                .additionalInfo(report.getAdditionalInfo())
                .status(report.getStatus())
                .reportedAt(report.getReportedAt())
                .resolvedAt(report.getResolvedAt())
                .build();
    }

    private void handleTemporaryBan(User user, int hours, String reason, CommentReportActionRequest.CommentBanTypes banTypes) {
        if (banTypes.isNoComment()) {
            user.setForumCommentBanned(true);
            user.setForumCommentBanExpiresAt(LocalDateTime.now().plusHours(hours));
        }
        userRepository.save(user);
    }

    private void handlePermanentBan(User user, String reason, CommentReportActionRequest.CommentBanTypes banTypes) {
        if (banTypes.isNoComment()) {
            user.setForumCommentBanned(true);
            user.setForumCommentBanExpiresAt(null);
        }
        userRepository.save(user);
    }
}