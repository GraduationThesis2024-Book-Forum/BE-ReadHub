package com.iuh.fit.readhub.services;

import com.iuh.fit.readhub.constants.NotificationType;
import com.iuh.fit.readhub.constants.ReportAction;
import com.iuh.fit.readhub.constants.ReportReason;
import com.iuh.fit.readhub.constants.ReportStatus;
import com.iuh.fit.readhub.dto.DiscussionDTO;
import com.iuh.fit.readhub.dto.DiscussionInteractionDTO;
import com.iuh.fit.readhub.dto.request.ForumRequest;
import com.iuh.fit.readhub.dto.request.ReportActionRequest;
import com.iuh.fit.readhub.exceptions.ForumException;
import com.iuh.fit.readhub.mapper.UserMapper;
import com.iuh.fit.readhub.models.*;
import com.iuh.fit.readhub.repositories.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Slf4j
@Transactional
public class DiscussionService {
    private final DiscussionRepository discussionRepository;
    private final DiscussionMemberRepository discussionMemberRepository;
    private final UserMapper userMapper;
    private final S3Service s3Service;
    private final DiscussionLikeRepository discussionLikeRepository;
    private final DiscussionSaveRepository discussionSaveRepository;
    private final DiscussionReportRepository discussionReportRepository;
    private final UserService userService;
    private final CommentDiscussionLikeRepository commentDiscussionLikeRepository;
    private final CommentDiscussionReplyRepository commentDiscussionReplyRepository;
    private final CommentRepository commentRepository;
    private final UserRepository userRepository;
    private final FCMService fcmService;
    private final CommentReportRepository commentReportRepository;

    public DiscussionService(DiscussionRepository discussionRepository,
                             DiscussionMemberRepository discussionMemberRepository,
                             UserMapper userMapper,
                             S3Service s3Service, DiscussionLikeRepository discussionLikeRepository, DiscussionSaveRepository discussionSaveRepository, DiscussionReportRepository discussionReportRepository, UserService userService, CommentDiscussionLikeRepository commentDiscussionLikeRepository, CommentDiscussionReplyRepository commentDiscussionReplyRepository, CommentRepository commentRepository, UserRepository userRepository, FCMService fcmService, CommentReportRepository commentReportRepository) {
        this.discussionRepository = discussionRepository;
        this.userMapper = userMapper;
        this.s3Service = s3Service;
        this.discussionLikeRepository = discussionLikeRepository;
        this.discussionMemberRepository = discussionMemberRepository;
        this.discussionReportRepository = discussionReportRepository;
        this.discussionSaveRepository = discussionSaveRepository;
        this.userService = userService;
        this.commentDiscussionLikeRepository = commentDiscussionLikeRepository;
        this.commentDiscussionReplyRepository = commentDiscussionReplyRepository;
        this.commentRepository = commentRepository;
        this.userRepository = userRepository;
        this.fcmService = fcmService;
        this.commentReportRepository = commentReportRepository;
    }

    public DiscussionInteractionDTO toggleLike(Long forumId, User user) {
        try {
            validateForumInteraction(user);
            Discussion discussion = discussionRepository.findById(forumId)
                    .orElseThrow(() -> new RuntimeException("Forum not found"));

            boolean exists = discussionLikeRepository.existsByDiscussionAndUser(discussion, user);

            if (exists) {
                discussionLikeRepository.deleteByDiscussionAndUser(discussion, user);
            } else {
                DiscussionLike like = DiscussionLike.builder()
                        .discussion(discussion)
                        .user(user)
                        .build();
                discussionLikeRepository.save(like);

                // Thêm notification khi có like mới
                Map<String, String> data = Map.of(
                        "type", NotificationType.FORUM_LIKE.name(),
                        "forumId", discussion.getDiscussionId().toString(),
                        "userId", user.getUserId().toString()
                );

                fcmService.sendNotification(
                        discussion.getCreator().getUserId(),
                        "New Forum Like",
                        user.getUsername() + " liked your forum: " + discussion.getForumTitle(),
                        data
                );
            }

            // Tính toán lại các tương tác sau khi thay đổi
            boolean isLiked = discussionLikeRepository.existsByDiscussionAndUser(discussion, user);
            boolean isSaved = discussionSaveRepository.existsByDiscussionAndUser(discussion, user);
            long likeCount = discussionLikeRepository.countByDiscussion(discussion);

            return DiscussionInteractionDTO.builder()
                    .isLiked(isLiked)
                    .isSaved(isSaved)
                    .likeCount(likeCount)
                    .build();

        } catch (Exception e) {
            throw new ForumException("Không thể thực hiện thao tác này");
        }
    }

    public DiscussionInteractionDTO toggleSave(Long forumId, User user) {
        validateForumInteraction(user);
        Discussion discussion = discussionRepository.findById(forumId)
                .orElseThrow(() -> new RuntimeException("Forum not found"));

        boolean exists = discussionSaveRepository.existsByDiscussionAndUser(discussion, user);
        if (exists) {
            discussionSaveRepository.deleteByDiscussionAndUser(discussion, user);
        } else {
            DiscussionSave discussionSave = DiscussionSave.builder()
                    .discussion(discussion)
                    .user(user)
                    .build();
            discussionSaveRepository.save(discussionSave);
        }

        return getForumInteractions(forumId, user);
    }

    public DiscussionInteractionDTO getForumInteractions(Long forumId, User user) {
        Discussion discussion = discussionRepository.findById(forumId)
                .orElseThrow(() -> new RuntimeException("Forum not found"));

        boolean isLiked = user != null &&
                discussionLikeRepository.existsByDiscussionAndUser(discussion, user);
        boolean isSaved = user != null &&
                discussionLikeRepository.existsByDiscussionAndUser(discussion, user);
        long likeCount = discussionLikeRepository.countByDiscussion(discussion);

        return DiscussionInteractionDTO.builder()
                .isLiked(isLiked)
                .isSaved(isSaved)
                .likeCount(likeCount)
                .build();
    }

    @Transactional
    public DiscussionDTO createForum(ForumRequest request, User creator) {
        validateCreationPermission(creator);
        if (creator.isCurrentlyBanned()) {
            String banMessage = creator.getForumCreationBanExpiresAt() != null ?
                    String.format("Bạn bị cấm tạo diễn đàn đến %s",
                            creator.getForumCreationBanExpiresAt()) :
                    "Bạn đã bị cấm tạo diễn đàn vĩnh viễn";

            throw new RuntimeException(banMessage);
        }
        String imageUrl = null;
        if (request.getForumImage() != null && !request.getForumImage().isEmpty()) {
            imageUrl = s3Service.uploadFile(request.getForumImage());
        }

        Discussion forum = Discussion.builder()
                .bookId(request.getBookId())
                .bookTitle(request.getBookTitle())
                .authors(request.getAuthors())
                .forumTitle(request.getForumTitle())
                .forumDescription(request.getForumDescription())
                .imageUrl(imageUrl)
                .subjects(request.getSubjects())
                .categories(request.getCategories())
                .creator(creator)
                .build();

        Discussion savedForum = discussionRepository.save(forum);
        DiscussionMember member = DiscussionMember.builder()
                .discussion(savedForum)
                .user(creator)
                .build();
        discussionMemberRepository.save(member);
        return convertToDTO(savedForum);
    }

    @Transactional
    public DiscussionDTO joinForum(Long forumId, User user) {
        validateJoinPermission(user);
        Discussion discussion = discussionRepository.findById(forumId)
                .orElseThrow(() -> new ForumException("Diễn đàn không tồn tại"));

        if (discussionMemberRepository.existsByDiscussion_DiscussionIdAndUser_UserId(forumId, user.getUserId())) {
            throw new ForumException("Bạn đã là thành viên của diễn đàn này");
        }

        DiscussionMember member = DiscussionMember.builder()
                .discussion(discussion)
                .user(user)
                .build();

        discussionMemberRepository.save(member);

        // Add notification for forum creator
        Map<String, String> data = Map.of(
                "type", NotificationType.NEW_MEMBER.name(),
                "forumId", discussion.getDiscussionId().toString(),
                "userId", user.getUserId().toString()
        );

        fcmService.sendNotification(
                discussion.getCreator().getUserId(),
                "New Forum Member",
                user.getUsername() + " joined your forum: " + discussion.getForumTitle(),
                data
        );

        Discussion updatedDiscussion = discussionRepository.findById(forumId)
                .orElseThrow(() -> new ForumException("Không thể cập nhật thông tin diễn đàn"));

        return convertToDTO(updatedDiscussion);
    }

    public List<DiscussionDTO> getAllForums() {
        return discussionRepository.findAllByOrderByCreatedAtDesc().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    private DiscussionDTO convertToDTO(Discussion discussion) {
        // Tối ưu hóa việc lấy số lượng
        long membersCount = discussionMemberRepository.countByDiscussion_DiscussionId(discussion.getDiscussionId());

        return DiscussionDTO.builder()
                .discussionId(discussion.getDiscussionId())
                .forumTitle(discussion.getForumTitle())
                .forumDescription(discussion.getForumDescription())
                .imageUrl(discussion.getImageUrl())
                .bookTitle(discussion.getBookTitle())
                .authors(discussion.getAuthors())
                .subjects(discussion.getSubjects() != null ? new ArrayList<>(discussion.getSubjects()) : new ArrayList<>())
                .categories(discussion.getCategories() != null ? new ArrayList<>(discussion.getCategories()) : new ArrayList<>())
                .creator(userMapper.toDTO(discussion.getCreator()))
                .totalMembers((int) membersCount)
                .totalPosts(discussion.getComments() != null ? discussion.getComments().size() : 0)
                .createdAt(discussion.getCreatedAt())
                .updatedAt(discussion.getUpdatedAt())
                .trending(discussion.getComments() != null && discussion.getComments().size() > 10)
                .build();
    }

    public boolean isForumMember(Long forumId, Long userId) {
        try {
            return discussionMemberRepository.existsByDiscussion_DiscussionIdAndUser_UserId(forumId, userId);
        } catch (Exception e) {
            return false;
        }
    }

    public DiscussionDTO getForumById(Long forumId) {
        Discussion forum = discussionRepository.findById(forumId)
                .orElseThrow(() -> new ForumException("Không tìm thấy diễn đàn"));
        return convertToDTO(forum);
    }

    @Transactional
    public void deleteForum(Long forumId) {
        try {
            Discussion forum = discussionRepository.findByIdWithLock(forumId)
                    .orElseThrow(() -> new ForumException("Not Found Forum"));

            // First delete all reports related to comments
            if (forum.getComments() != null) {
                for (Comment comment : new ArrayList<>(forum.getComments())) {
                    // Delete comment reports first
                    commentReportRepository.deleteByComment(comment);
                    commentReportRepository.flush();

                    // Then delete comment likes and replies
                    commentDiscussionLikeRepository.deleteByComment(comment);
                    commentDiscussionReplyRepository.deleteByParentComment(comment);
                }
                // After deleting all comment-related data, delete the comments themselves
                commentRepository.deleteByDiscussion(forum);
                commentRepository.flush();
            }

            // Delete forum reports
            discussionReportRepository.deleteByDiscussion(forum);
            discussionReportRepository.flush();

            // Delete forum-related data
            discussionMemberRepository.deleteByDiscussion(forum);
            discussionLikeRepository.deleteByDiscussion(forum);
            discussionSaveRepository.deleteByDiscussion(forum);

            // Clear relationships
            forum.getMembers().clear();
            forum.getLikes().clear();
            forum.getSaves().clear();
            forum.setCreator(null);
            forum.getComments().clear();

            // Finally delete the forum
            discussionRepository.delete(forum);
            discussionRepository.flush();
        } catch (Exception e) {
            throw new ForumException("Can't delete Forum: " + e.getMessage());
        }
    }

    @Transactional
    public void reportForum(Long forumId, User reporter, ReportReason reason, String additionalInfo) {
        try {
            Discussion forum = discussionRepository.findById(forumId)
                    .orElseThrow(() -> new ForumException("Forum not found"));

            if (forum.getCreator().getUserId().equals(reporter.getUserId())) {
                throw new ForumException("You cannot report your own forum");
            }

            DiscussionReport report = DiscussionReport.builder()
                    .discussion(forum)
                    .reporter(reporter)
                    .reason(reason)
                    .additionalInfo(additionalInfo)
                    .reportedAt(LocalDateTime.now())
                    .status(ReportStatus.PENDING)
                    .build();

            discussionReportRepository.save(report);

            // Notify admins
            List<User> admins = userService.getAllAdmins();
            for (User admin : admins) {
                // Data cho notification
                Map<String, String> notificationData = Map.of(
                        "type", NotificationType.FORUM_REPORT.name(),
                        "forumId", forumId.toString(),
                        "reportId", report.getId().toString(), // Thêm reportId
                        "reporterId", reporter.getUserId().toString(),
                        "reason", reason.name()
                );

                // Tạo nội dung thông báo chi tiết hơn
                String notificationMessage = String.format(
                        "Forum '%s' has been reported by %s for %s",
                        forum.getForumTitle(),
                        reporter.getFullName(),
                        reason.getDescription()
                );

                // Gửi và lưu notification
                fcmService.sendNotification(
                        admin.getUserId(),
                        "New Forum Report", // Title rõ ràng hơn
                        notificationMessage,
                        notificationData
                );
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new ForumException("Error reporting forum: " + e.getMessage());
        }
    }

    public boolean isForumCreator(Long forumId, Authentication authentication) {
        User currentUser = userService.getCurrentUser(authentication);
        Discussion forum = discussionRepository.findById(forumId)
                .orElseThrow(() -> new ForumException("Diễn đàn không tồn tại"));
        return forum.getCreator().getUserId().equals(currentUser.getUserId());
    }

    @Transactional(isolation = Isolation.SERIALIZABLE)
    public DiscussionReport handleReportAction(Long reportId, ReportActionRequest request) {
        try {
            // Load report với lock
            DiscussionReport mainReport = discussionReportRepository.findByIdWithLock(reportId)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy báo cáo"));

            if (mainReport.getStatus() != ReportStatus.PENDING) {
                throw new RuntimeException("Report đã được xử lý");
            }

            // Kiểm tra và lấy thông tin discussion và creator
            Discussion discussion = mainReport.getDiscussion();
            if (discussion == null) {
                throw new RuntimeException("Không tìm thấy diễn đàn của báo cáo này");
            }

            // Get all pending reports for this forum
            List<DiscussionReport> relatedReports = discussionReportRepository
                    .findByDiscussionAndStatus(discussion, ReportStatus.PENDING);

            User forumCreator = discussion.getCreator();
            if (forumCreator == null) {
                throw new RuntimeException("Diễn đàn này không có người tạo");
            }

            // Lưu lại userId của creator để gửi thông báo sau này
            Long creatorUserId = forumCreator.getUserId();

            // Lock user để update
            forumCreator = userRepository.findByIdWithLock(forumCreator.getUserId())
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy người tạo diễn đàn"));

            // Prepare notification message and data first
            String notificationMessage;
            Map<String, String> notificationData = new HashMap<>();
            notificationData.put("type", NotificationType.REPORT_ACTION.name());
            notificationData.put("reportId", reportId.toString());
            notificationData.put("action", request.getAction().name());
            notificationData.put("forumId", discussion.getDiscussionId().toString());

            // Update all related reports with the same action
            for (DiscussionReport report : relatedReports) {
                updateReportStatus(report, request.getAction());
                discussionReportRepository.save(report);

                // Notify the reporter
                notifyReporter(report, request.getAction(), request.getReason());
            }

            // Rest of the action handling (ban, warning, etc.) remains the same
            if (request.getBanTypes() != null) {
                notificationData.put("noInteraction", String.valueOf(request.getBanTypes().isNoInteraction()));
                notificationData.put("noComment", String.valueOf(request.getBanTypes().isNoComment()));
                notificationData.put("noJoin", String.valueOf(request.getBanTypes().isNoJoin()));
            }

            if (request.getAction().toString().startsWith("BAN_")) {
                String duration = request.getAction() == ReportAction.BAN_PERMANENT ?
                        "permanently" :
                        "for " + request.getAction().getBanHours() + " hours";

                List<String> restrictions = new ArrayList<>();
                if (request.getBanTypes().isNoInteraction()) restrictions.add("forum interactions");
                if (request.getBanTypes().isNoComment()) restrictions.add("commenting");
                if (request.getBanTypes().isNoJoin()) restrictions.add("joining forums");

                String restrictionsText = String.join(", ", restrictions);
                notificationMessage = String.format(
                        "You have been banned %s from: %s. Reason: %s",
                        duration,
                        restrictionsText,
                        request.getReason()
                );
            } else {
                notificationMessage = request.getAction().getNotificationMessage();
            }

            // Handle the specific action
            switch (request.getAction()) {
                case DISMISS:
                    mainReport.setStatus(ReportStatus.DISMISSED);
                    break;

                case WARN:
                    mainReport.setStatus(ReportStatus.WARNED);
                    handleWarn(forumCreator, request.getReason());
                    break;

                case BAN_1H:
                case BAN_3H:
                case BAN_24H:
                    mainReport.setStatus(ReportStatus.BANNED);
                    handleTemporaryBan(forumCreator, request.getAction().getBanHours(), request.getReason(), request.getBanTypes());
                    break;

                case BAN_PERMANENT:
                    mainReport.setStatus(ReportStatus.BANNED);
                    handlePermanentBan(forumCreator, request.getReason(), request.getBanTypes());
                    break;
            }

            mainReport.setResolvedAt(LocalDateTime.now());
            DiscussionReport savedReport = discussionReportRepository.save(mainReport);

            // Xóa forum sau cùng nếu được yêu cầu
            if (request.getBanTypes() != null && request.getBanTypes().isDeleteForum()) {
                deleteForum(discussion.getDiscussionId());
            }

            return savedReport;
        } catch (Exception e) {
            throw new RuntimeException("Lỗi khi xử lý báo cáo: " + e.getMessage(), e);
        }
    }

    private void updateReportStatus(DiscussionReport report, ReportAction action) {
        switch (action) {
            case DISMISS:
                report.setStatus(ReportStatus.DISMISSED);
                break;
            case WARN:
                report.setStatus(ReportStatus.WARNED);
                break;
            case BAN_1H:
            case BAN_3H:
            case BAN_24H:
            case BAN_PERMANENT:
                report.setStatus(ReportStatus.BANNED);
                break;
        }
        report.setResolvedAt(LocalDateTime.now());
    }

    private void notifyReporter(DiscussionReport report, ReportAction action, String reason) {
        try {
            String title;
            String message;
            Map<String, String> data = new HashMap<>();
            data.put("type", NotificationType.REPORT_ACTION.name());
            data.put("reportId", report.getId().toString());
            data.put("action", action.name());
            data.put("forumId", report.getDiscussion().getDiscussionId().toString());

            switch (action) {
                case DISMISS:
                    title = "Report Update: Dismissed";
                    message = String.format(
                            "Your report for forum '%s' has been reviewed and dismissed by moderators.",
                            report.getDiscussion().getForumTitle()
                    );
                    break;

                case WARN:
                    title = "Report Update: Warning Issued";
                    message = String.format(
                            "Your report for forum '%s' has been reviewed. A warning has been issued to the forum creator. Reason: %s",
                            report.getDiscussion().getForumTitle(),
                            reason
                    );
                    break;

                case BAN_1H:
                case BAN_3H:
                case BAN_24H:
                    title = "Report Update: Temporary Ban";
                    message = String.format(
                            "Your report for forum '%s' has been reviewed. The forum creator has been temporarily banned for %d hours. Reason: %s",
                            report.getDiscussion().getForumTitle(),
                            action.getBanHours(),
                            reason
                    );
                    data.put("banHours", String.valueOf(action.getBanHours()));
                    break;

                case BAN_PERMANENT:
                    title = "Report Update: Permanent Ban";
                    message = String.format(
                            "Your report for forum '%s' has been reviewed. The forum creator has been permanently banned. Reason: %s",
                            report.getDiscussion().getForumTitle(),
                            reason
                    );
                    break;

                default:
                    title = "Report Update";
                    message = String.format(
                            "Your report for forum '%s' has been reviewed and processed.",
                            report.getDiscussion().getForumTitle()
                    );
            }

            // Add common data
            data.put("forumTitle", report.getDiscussion().getForumTitle());
            data.put("reason", reason);

            // Send notification to the reporter
            fcmService.sendNotification(
                    report.getReporter().getUserId(),
                    title,
                    message,
                    data
            );

            // Log the notification
            log.info("Sent report action notification to reporter ID: {} for report ID: {} with action: {}",
                    report.getReporter().getUserId(), report.getId(), action);

        } catch (Exception e) {
            // Log error but don't throw to avoid disrupting the main flow
            log.error("Failed to send notification to reporter ID: {} for report ID: {}. Error: {}",
                    report.getReporter().getUserId(), report.getId(), e.getMessage(), e);
        }
    }

    private void handleTemporaryBan(User user, int hours, String reason, ReportActionRequest.BanTypes banTypes) {
        try {
            // Set các trạng thái ban cho user
            if (banTypes.isNoInteraction()) {
                user.setForumInteractionBanned(true);
                user.setForumBanExpiresAt(LocalDateTime.now().plusHours(hours));
                user.setForumBanReason(reason);
            }

            if (banTypes.isNoComment()) {
                user.setForumCommentBanned(true);
                user.setForumCommentBanExpiresAt(LocalDateTime.now().plusHours(hours));
            }

            if (banTypes.isNoJoin()) {
                user.setForumJoinBanned(true);
                user.setForumJoinBanExpiresAt(LocalDateTime.now().plusHours(hours));
            }

            if (banTypes.isNoForumCreation()) {
                user.setForumCreationBanned(true);
                user.setForumCreationBanExpiresAt(LocalDateTime.now().plusHours(hours));
                user.setForumCreationBanReason(reason);
            }

            // Lưu user
            userRepository.saveAndFlush(user);

            // Gửi thông báo
            Map<String, String> notificationData = Map.of(
                    "type", NotificationType.BAN.name(),
                    "userId", user.getUserId().toString(),
                    "duration", String.valueOf(hours),
                    "reason", reason,
                    "banTypes", getBanTypesString(banTypes)
            );

            String message = formatBanMessage(hours, reason, banTypes);
            fcmService.sendNotification(
                    user.getUserId(),
                    NotificationType.BAN.getTitle(),
                    message,
                    notificationData
            );
        } catch (Exception e) {
            throw new RuntimeException("Failed to apply temporary ban: " + e.getMessage());
        }
    }

    private void handlePermanentBan(User user, String reason, ReportActionRequest.BanTypes banTypes) {
        try {
            if (banTypes.isNoInteraction()) {
                user.setForumInteractionBanned(true);
                user.setForumBanExpiresAt(null); // Permanent
                user.setForumBanReason(reason);
            }

            if (banTypes.isNoComment() || banTypes.isNoJoin()) {
                user.setForumCreationBanned(true);
                user.setForumCreationBanExpiresAt(null); // Permanent
                user.setForumCreationBanReason(reason);
            }

            if (banTypes.isNoForumCreation()) { // Thêm xử lý cho permanent ban tạo diễn đàn
                user.setForumCreationBanned(true);
                user.setForumCreationBanExpiresAt(null);
                user.setForumCreationBanReason(reason);
            }

            userRepository.saveAndFlush(user);

            Map<String, String> notificationData = Map.of(
                    "type", NotificationType.PERMANENT_BAN.name(),
                    "userId", user.getUserId().toString(),
                    "reason", reason,
                    "banTypes", getBanTypesString(banTypes)
            );

            String message = formatPermanentBanMessage(reason, banTypes);
            fcmService.sendNotification(
                    user.getUserId(),
                    NotificationType.PERMANENT_BAN.getTitle(),
                    message,
                    notificationData
            );
        } catch (Exception e) {
            throw new RuntimeException("Failed to apply permanent ban");
        }
    }

    private void handleWarn(User user, String reason) {
        try {
            Map<String, String> notificationData = Map.of(
                    "type", NotificationType.WARNING.name(),
                    "userId", user.getUserId().toString(),
                    "reason", reason
            );

            String message = NotificationType.WARNING.formatMessage(reason);
            fcmService.sendNotification(
                    user.getUserId(),
                    NotificationType.WARNING.getTitle(),
                    message,
                    notificationData
            );
        } catch (Exception e) {
            throw new RuntimeException("Failed to send warning");
        }
    }

    // Method to check comment permissions
    public boolean canInteractWithForum(User user) {
        if (user.getForumInteractionBanned()) {
            LocalDateTime banExpiry = user.getForumBanExpiresAt();
            if (banExpiry == null) {
                return false; // Permanent ban
            }
            return LocalDateTime.now().isAfter(banExpiry);
        }
        return true;
    }

    public void validateForumInteraction(User user) {
        if (!canInteractWithForum(user)) {
            String banMessage = user.getForumBanExpiresAt() != null ?
                    String.format("You are banned from forum interactions until %s", user.getForumBanExpiresAt()) :
                    "You have been permanently banned from forum interactions";

            // Send notification about attempted interaction while banned
            Map<String, String> notificationData = Map.of(
                    "type", NotificationType.BAN.name(),
                    "userId", user.getUserId().toString(),
                    "bannedUntil", user.getForumBanExpiresAt() != null ?
                            user.getForumBanExpiresAt().toString() : "PERMANENT"
            );

            fcmService.sendNotification(
                    user.getUserId(),
                    "Forum Access Restricted",
                    banMessage,
                    notificationData
            );

            throw new RuntimeException(banMessage);
        }

        // If ban has expired, remove the ban status
        if (Boolean.TRUE.equals(user.getForumInteractionBanned()) &&
                user.getForumBanExpiresAt() != null &&
                user.getForumBanExpiresAt().isBefore(LocalDateTime.now())) {

            user.setForumInteractionBanned(false);
            user.setForumBanReason(null);
            user.setForumBanExpiresAt(null);
            userRepository.save(user);

            // Send notification about ban expiry
            Map<String, String> notificationData = Map.of(
                    "type", NotificationType.BAN_EXPIRED.name(),
                    "userId", user.getUserId().toString()
            );

            fcmService.sendNotification(
                    user.getUserId(),
                    "Forum Access Restored",
                    "Your forum interaction restrictions have been lifted. You can now participate in forums again.",
                    notificationData
            );
        }
    }

    public void unbanUser(User user) {
        user.setForumCreationBanned(false);
        user.setForumCreationBanReason(null);
        user.setForumCreationBanExpiresAt(null);
        userRepository.save(user);
    }

    private String getBanTypesString(ReportActionRequest.BanTypes banTypes) {
        List<String> restrictions = new ArrayList<>();
        if (banTypes.isNoInteraction()) restrictions.add("forum interactions");
        if (banTypes.isNoComment()) restrictions.add("commenting");
        if (banTypes.isNoJoin()) restrictions.add("joining forums");
        return String.join(", ", restrictions);
    }

    private String formatBanMessage(int hours, String reason, ReportActionRequest.BanTypes banTypes) {
        String restrictions = getBanTypesString(banTypes);
        return String.format("You have been banned from %s for %d hours. Reason: %s",
                restrictions, hours, reason);
    }

    private String formatPermanentBanMessage(String reason, ReportActionRequest.BanTypes banTypes) {
        String restrictions = getBanTypesString(banTypes);
        return String.format("You have been permanently banned from %s. Reason: %s",
                restrictions, reason);
    }

    private void validateCommentPermission(User user) {
        if (Boolean.TRUE.equals(user.getForumCommentBanned())) {
            if (user.getForumCommentBanExpiresAt() == null) {
                throw new RuntimeException("You have been permanently banned from commenting");
            }
            if (user.getForumCommentBanExpiresAt().isAfter(LocalDateTime.now())) {
                throw new RuntimeException("You are banned from commenting until " + user.getForumCommentBanExpiresAt());
            }
        }
    }

    private void validateJoinPermission(User user) {
        if (Boolean.TRUE.equals(user.getForumJoinBanned())) {
            if (user.getForumJoinBanExpiresAt() == null) {
                throw new RuntimeException("You have been permanently banned from joining forums");
            }
            if (user.getForumJoinBanExpiresAt().isAfter(LocalDateTime.now())) {
                throw new RuntimeException("You are banned from joining forums until " + user.getForumJoinBanExpiresAt());
            }
        }
    }

    private void validateCreationPermission(User user) {
        if (Boolean.TRUE.equals(user.getForumCreationBanned())) {
            if (user.getForumCreationBanExpiresAt() == null) {
                throw new RuntimeException("You have been permanently banned from creating forums");
            }
            if (user.getForumCreationBanExpiresAt().isAfter(LocalDateTime.now())) {
                throw new RuntimeException("You are banned from creating forums until " + user.getForumCreationBanExpiresAt());
            }
        }
    }

    private void validateInteractionPermission(User user) {
        if (Boolean.TRUE.equals(user.getForumInteractionBanned())) {
            if (user.getForumBanExpiresAt() == null) {
                throw new RuntimeException("You have been permanently banned from forum interactions");
            }
            if (user.getForumBanExpiresAt().isAfter(LocalDateTime.now())) {
                throw new RuntimeException("You are banned from forum interactions until " + user.getForumBanExpiresAt());
            }
        }
    }
}