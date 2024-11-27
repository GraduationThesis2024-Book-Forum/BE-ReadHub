package com.iuh.fit.readhub.services;

import com.iuh.fit.readhub.constants.NotificationType;
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
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
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

    public DiscussionService(DiscussionRepository discussionRepository,
                             DiscussionMemberRepository discussionMemberRepository,
                             UserMapper userMapper,
                             S3Service s3Service, DiscussionLikeRepository discussionLikeRepository, DiscussionSaveRepository discussionSaveRepository, DiscussionReportRepository discussionReportRepository, UserService userService, CommentDiscussionLikeRepository commentDiscussionLikeRepository, CommentDiscussionReplyRepository commentDiscussionReplyRepository, CommentRepository commentRepository, UserRepository userRepository, FCMService fcmService) {
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
        validateForumInteraction(creator);
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
        validateForumInteraction(user);
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
            // Lấy forum với lock
            Discussion forum = discussionRepository.findByIdWithLock(forumId)
                    .orElseThrow(() -> new ForumException("Not Found Forum"));

            // Xóa các bảng liên quan
            discussionMemberRepository.deleteByDiscussion(forum);
            discussionLikeRepository.deleteByDiscussion(forum);
            discussionSaveRepository.deleteByDiscussion(forum);
            discussionReportRepository.deleteByDiscussion(forum);

            // Xóa các comment và reply
            if (forum.getComments() != null) {
                for (Comment comment : new ArrayList<>(forum.getComments())) {
                    commentDiscussionLikeRepository.deleteByComment(comment);
                    commentDiscussionReplyRepository.deleteByParentComment(comment);
                    forum.getComments().remove(comment);
                    commentRepository.delete(comment);
                }
            }

            // Clear all relationships
            forum.getMembers().clear();
            forum.getLikes().clear();
            forum.getSaves().clear();
            forum.setCreator(null);

            // Save changes
            discussionRepository.saveAndFlush(forum);

            // Delete forum
            discussionRepository.delete(forum);
            discussionRepository.flush();
        } catch (Exception e) {
            throw new ForumException("Can't delete Forum " + e.getMessage());
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
            DiscussionReport report = discussionReportRepository.findByIdWithLock(reportId)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy báo cáo"));

            // Kiểm tra và lấy thông tin discussion và creator
            Discussion discussion = report.getDiscussion();
            if (discussion == null) {
                throw new RuntimeException("Không tìm thấy diễn đàn của báo cáo này");
            }

            User forumCreator = discussion.getCreator();
            if (forumCreator == null) {
                throw new RuntimeException("Diễn đàn này không có người tạo");
            }

            // Lock user để update
            forumCreator = userRepository.findByIdWithLock(forumCreator.getUserId())
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy người tạo diễn đàn"));

            // Xử lý xóa forum trước nếu được yêu cầu
            if (request.getBanTypes() != null && request.getBanTypes().isDeleteForum()) {
                deleteForum(discussion.getDiscussionId());
            }

            // Xử lý warning hoặc ban
            switch (request.getAction()) {
                case DISMISS:
                    report.setStatus(ReportStatus.DISMISSED);
                    break;

                case WARN:
                    report.setStatus(ReportStatus.WARNED);
                    handleWarn(forumCreator, request.getReason());
                    break;

                case BAN_1H:
                case BAN_3H:
                case BAN_24H:
                    handleTemporaryBan(forumCreator, request.getAction().getBanHours(), request.getReason(), request.getBanTypes());
                    report.setStatus(ReportStatus.BANNED);
                    break;

                case BAN_PERMANENT:
                    handlePermanentBan(forumCreator, request.getReason(), request.getBanTypes());
                    report.setStatus(ReportStatus.BANNED);
                    break;
            }

            report.setResolvedAt(LocalDateTime.now());
            return  discussionReportRepository.save(report);

        } catch (Exception e) {
            throw new RuntimeException("Lỗi khi xử lý báo cáo: " + e.getMessage(), e);
        }
    }

    private void handleTemporaryBan(User user, int hours, String reason, ReportActionRequest.BanTypes banTypes) {
        try {
            if (banTypes.isNoInteraction()) {
                user.setForumInteractionBanned(true);
                user.setForumBanExpiresAt(LocalDateTime.now().plusHours(hours));
                user.setForumBanReason(reason);
            }

            if (banTypes.isNoComment()) {
                user.setForumCommentBanned(true);
                user.setForumCommentBanExpiresAt(LocalDateTime.now().plusHours(hours));
            }

            if (banTypes.isNoForumCreation()) { // Thêm xử lý cho ban tạo diễn đàn
                user.setForumCreationBanned(true);
                user.setForumCreationBanExpiresAt(LocalDateTime.now().plusHours(hours));
                user.setForumCreationBanReason(reason);
            }

            userRepository.saveAndFlush(user);

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
            throw new RuntimeException("Failed to apply temporary ban");
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

    public void banUser(User user, String reason, Integer hours) {
        user.setForumCreationBanned(true);
        user.setForumCreationBanReason(reason);

        if (hours != null) {
            user.setForumCreationBanExpiresAt(LocalDateTime.now().plusHours(hours));
        } else {
            user.setForumCreationBanExpiresAt(null); // Permanent ban
        }

        userRepository.save(user);
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
}