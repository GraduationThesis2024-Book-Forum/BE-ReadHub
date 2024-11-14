package com.iuh.fit.readhub.services;

import com.iuh.fit.readhub.constants.ReportReason;
import com.iuh.fit.readhub.dto.ForumDTO;
import com.iuh.fit.readhub.dto.ForumInteractionDTO;
import com.iuh.fit.readhub.dto.request.ForumRequest;
import com.iuh.fit.readhub.exceptions.ForumException;
import com.iuh.fit.readhub.mapper.UserMapper;
import com.iuh.fit.readhub.models.*;
import com.iuh.fit.readhub.repositories.*;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class ForumService {
    private final ForumRepository forumRepository;
    private final ForumMemberRepository forumMemberRepository;
    private final UserMapper userMapper;
    private final S3Service s3Service;
    private final ForumLikeRepository forumLikeRepository;
    private final ForumSaveRepository forumSaveRepository;
    private final ForumReportRepository forumReportRepository;
    private final UserService userService;
    private final CommentDiscussionLikeRepository commentDiscussionLikeRepository;
    private final CommentDiscussionReplyRepository commentDiscussionReplyRepository;
    private final CommentRepository commentRepository;

    public ForumService(ForumRepository forumRepository,
                        ForumMemberRepository forumMemberRepository,
                        UserMapper userMapper,
                        S3Service s3Service, ForumLikeRepository forumLikeRepository, ForumSaveRepository forumSaveRepository, ForumReportRepository forumReportRepository, UserService userService, CommentDiscussionLikeRepository commentDiscussionLikeRepository, CommentDiscussionReplyRepository commentDiscussionReplyRepository, CommentRepository commentRepository) {
        this.forumRepository = forumRepository;
        this.forumMemberRepository = forumMemberRepository;
        this.userMapper = userMapper;
        this.s3Service = s3Service;
        this.forumLikeRepository = forumLikeRepository;
        this.forumSaveRepository = forumSaveRepository;
        this.forumReportRepository = forumReportRepository;
        this.userService = userService;
        this.commentDiscussionLikeRepository = commentDiscussionLikeRepository;
        this.commentDiscussionReplyRepository = commentDiscussionReplyRepository;
        this.commentRepository = commentRepository;
    }

    public ForumInteractionDTO toggleLike(Long forumId, User user) {
        try {
            Discussion discussion = forumRepository.findById(forumId)
                    .orElseThrow(() -> new RuntimeException("Forum not found"));

            boolean exists = forumLikeRepository.existsByDiscussionAndUser(discussion, user);
            if (exists) {
                forumLikeRepository.deleteByDiscussionAndUser(discussion, user);
            } else {
                ForumLike like = ForumLike.builder()
                        .discussion(discussion)
                        .user(user)
                        .build();
                forumLikeRepository.save(like);
            }

            return getForumInteractions(forumId, user);
        } catch (Exception e) {
            throw new ForumException("Không thể thực hiện thao tác này");
        }

    }

    public ForumInteractionDTO toggleSave(Long forumId, User user) {
        Discussion discussion = forumRepository.findById(forumId)
                .orElseThrow(() -> new RuntimeException("Forum not found"));

        boolean exists = forumSaveRepository.existsByDiscussionAndUser(discussion, user);
        if (exists) {
            forumSaveRepository.deleteByDiscussionAndUser(discussion, user);
        } else {
            ForumSave save = ForumSave.builder()
                    .discussion(discussion)
                    .user(user)
                    .build();
            forumSaveRepository.save(save);
        }

        return getForumInteractions(forumId, user);
    }

    public ForumInteractionDTO getForumInteractions(Long forumId, User user) {
        Discussion discussion = forumRepository.findById(forumId)
                .orElseThrow(() -> new RuntimeException("Forum not found"));

        boolean isLiked = user != null &&
                forumLikeRepository.existsByDiscussionAndUser(discussion, user);
        boolean isSaved = user != null &&
                forumSaveRepository.existsByDiscussionAndUser(discussion, user);
        long likeCount = forumLikeRepository.countByDiscussion(discussion);

        return ForumInteractionDTO.builder()
                .isLiked(isLiked)
                .isSaved(isSaved)
                .likeCount(likeCount)
                .build();
    }

    @Transactional
    public ForumDTO createForum(ForumRequest request, User creator) {
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

        Discussion savedForum = forumRepository.save(forum);
        return convertToDTO(savedForum);
    }

    @Transactional
    public ForumDTO joinForum(Long forumId, User user) {
        Discussion discussion = forumRepository.findById(forumId)
                .orElseThrow(() -> new ForumException("Diễn đàn không tồn tại"));

        // Kiểm tra xem user đã join chưa
        if (forumMemberRepository.existsByDiscussion_DiscussionIdAndUser_UserId(forumId, user.getUserId())) {
            throw new ForumException("Bạn đã là thành viên của diễn đàn này");
        }

        // Tạo ForumMember mới
        ForumMember member = ForumMember.builder()
                .discussion(discussion)
                .user(user)
                .build();

        forumMemberRepository.save(member);

        // Refresh và convert to DTO
        Discussion updatedDiscussion = forumRepository.findById(forumId)
                .orElseThrow(() -> new ForumException("Không thể cập nhật thông tin diễn đàn"));

        return convertToDTO(updatedDiscussion);
    }

    public List<ForumDTO> getAllForums() {
        return forumRepository.findAllByOrderByCreatedAtDesc().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    private ForumDTO convertToDTO(Discussion discussion) {
        // Tối ưu hóa việc lấy số lượng
        long membersCount = forumMemberRepository.countByDiscussion_DiscussionId(discussion.getDiscussionId());

        return ForumDTO.builder()
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
            return forumMemberRepository.existsByDiscussion_DiscussionIdAndUser_UserId(forumId, userId);
        } catch (Exception e) {
            return false;
        }
    }

    public ForumDTO getForumById(Long forumId) {
        Discussion forum = forumRepository.findById(forumId)
                .orElseThrow(() -> new ForumException("Không tìm thấy diễn đàn"));
        return convertToDTO(forum);
    }

    @Transactional
    public void deleteForum(Long forumId) {
        Discussion forum = forumRepository.findById(forumId)
                .orElseThrow(() -> new ForumException("Diễn đàn không tồn tại"));

        // Xóa các bảng liên quan
        forumMemberRepository.deleteByDiscussion(forum);
        forumLikeRepository.deleteByDiscussion(forum);
        forumSaveRepository.deleteByDiscussion(forum);

        // Xóa các comment và reply
        forum.getComments().forEach(comment -> {
            commentDiscussionLikeRepository.deleteByComment(comment);
            commentDiscussionReplyRepository.deleteByParentComment(comment);
            commentRepository.delete(comment);
        });

        // Xóa forum
        forumRepository.delete(forum);
    }

    @Transactional
    public void reportForum(Long forumId, User reporter, ReportReason reason, String additionalInfo) {
        try {
            Discussion forum = forumRepository.findById(forumId)
                    .orElseThrow(() -> new ForumException("Forum not found"));

            // Debug log
            System.out.println("Reason: " + reason);
            System.out.println("Additional Info: " + additionalInfo);

            ForumReport report = ForumReport.builder()
                    .forum(forum)
                    .reporter(reporter)
                    .reason(reason)
                    .additionalInfo(additionalInfo)
                    .reportedAt(LocalDateTime.now())
                    .status("PENDING")
                    .build();

            forumReportRepository.save(report);
        } catch (Exception e) {
            e.printStackTrace();
            throw new ForumException("Error reporting forum: " + e.getMessage());
        }
    }

    public boolean isForumCreator(Long forumId, Authentication authentication) {
        User currentUser = userService.getCurrentUser(authentication);
        Discussion forum = forumRepository.findById(forumId)
                .orElseThrow(() -> new ForumException("Diễn đàn không tồn tại"));
        return forum.getCreator().getUserId().equals(currentUser.getUserId());
    }
}