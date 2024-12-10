package com.iuh.fit.readhub.services;

import com.iuh.fit.readhub.constants.CommentReportReason;
import com.iuh.fit.readhub.constants.NotificationType;
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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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

        // Notify admins
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

    public void handleReportAction(Long reportId, CommentReportActionRequest request) {
        CommentReport report = commentReportRepository.findByIdWithLock(reportId)
                .orElseThrow(() -> new RuntimeException("Report not found"));

        if (report.getStatus() != ReportStatus.PENDING) {
            throw new RuntimeException("Report has already been handled");
        }

        Comment comment = report.getComment();
        User commentAuthor = comment.getUser();
        String notificationMessage;
        Map<String, String> notificationData = new HashMap<>();
        notificationData.put("type", NotificationType.REPORT_ACTION.name());
        notificationData.put("reportId", reportId.toString());
        notificationData.put("action", request.getAction().name());
        notificationData.put("commentId", comment.getCommentId().toString());

        switch (request.getAction()) {
            case DISMISS:
                report.setStatus(ReportStatus.DISMISSED);
                notificationMessage = "Your reported comment has been reviewed and no action was taken.";
                break;

            case BAN_1H:
            case BAN_3H:
            case BAN_24H:
                report.setStatus(ReportStatus.BANNED);
                handleTemporaryBan(commentAuthor, request.getAction().getBanHours(),
                        request.getReason(),
                        request.getBanTypes());
                notificationMessage = String.format("You have been banned for %d hours. Reason: %s",
                        request.getAction().getBanHours(), request.getReason());
                break;

            case BAN_PERMANENT:
                report.setStatus(ReportStatus.BANNED);
                handlePermanentBan(commentAuthor, request.getReason(), request.getBanTypes());
                notificationMessage = "You have been permanently banned. Reason: " + request.getReason();
                break;

            default:
                throw new RuntimeException("Invalid action");
        }

        if (request.getBanTypes() != null && request.getBanTypes().isDeleteComment()) {
            commentRepository.delete(comment);
            notificationMessage += " Your comment has been deleted.";
        }

        report.setResolvedAt(LocalDateTime.now());
        commentReportRepository.save(report);

        fcmService.sendNotification(
                commentAuthor.getUserId(),
                "Comment Report Action",
                notificationMessage,
                notificationData
        );
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