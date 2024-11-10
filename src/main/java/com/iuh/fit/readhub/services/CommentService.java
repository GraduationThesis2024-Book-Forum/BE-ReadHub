package com.iuh.fit.readhub.services;

import com.iuh.fit.readhub.dto.CommentDTO;
import com.iuh.fit.readhub.dto.CommentDiscussionReplyDTO;
import com.iuh.fit.readhub.dto.UserDTO;
import com.iuh.fit.readhub.dto.message.CommentMessage;
import com.iuh.fit.readhub.mapper.UserMapper;
import com.iuh.fit.readhub.models.*;
import com.iuh.fit.readhub.repositories.CommentDiscussionLikeRepository;
import com.iuh.fit.readhub.repositories.CommentDiscussionReplyRepository;
import com.iuh.fit.readhub.repositories.CommentRepository;
import com.iuh.fit.readhub.repositories.ForumRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CommentService {

    private final CommentRepository commentRepository;
    private final ForumRepository forumRepository;
    private final UserService userService;
    private final UserMapper userMapper;
    private final S3Service s3Service;
    private final CommentDiscussionLikeRepository commentDiscussionLikeRepository;
    private final CommentDiscussionReplyRepository commentDiscussionReplyRepository;

    @Transactional
    public CommentDTO createComment(CommentMessage message, Authentication authentication) {
        User currentUser = userService.getCurrentUser(authentication);
        Discussion discussion = forumRepository.findById(message.getDiscussionId())
                .orElseThrow(() -> new RuntimeException("Forum not found"));

        Comment comment = Comment.builder()
                .content(message.getContent())
                .imageUrl(message.getImageUrl())
                .discussion(discussion)
                .user(currentUser)
                .build();

        Comment savedComment = commentRepository.save(comment);
        return convertToDTO(savedComment, currentUser);
    }

    public List<CommentDTO> getForumComments(Long forumId) {
        List<Comment> comments = commentRepository.findByDiscussion_DiscussionIdOrderByCreatedAtDesc(forumId);

        User currentUser = null;
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null && authentication.isAuthenticated()) {
                currentUser = userService.getCurrentUser(authentication);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        final User finalCurrentUser = currentUser;
        return comments.stream()
                .map(comment -> convertToDTO(comment, finalCurrentUser))
                .collect(Collectors.toList());
    }

    @Transactional
    public boolean toggleLike(Long commentId, Authentication authentication) {
        User user = userService.getCurrentUser(authentication);
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new RuntimeException("Comment not found"));

        boolean exists = commentDiscussionLikeRepository.existsByCommentAndUser(comment, user);
        if (exists) {
            commentDiscussionLikeRepository.deleteByCommentAndUser(comment, user);
            return false;
        } else {
            CommentDiscussionLike like = CommentDiscussionLike.builder()
                    .comment(comment)
                    .user(user)
                    .build();
            commentDiscussionLikeRepository.save(like);
            return true;
        }
    }

    @Transactional
    public CommentDiscussionReplyDTO createReply(Long commentId, String content, String imageUrl, Authentication authentication) {
        User user = userService.getCurrentUser(authentication);
        Comment parentComment = commentRepository.findById(commentId)
                .orElseThrow(() -> new RuntimeException("Comment not found"));

        CommentDiscussionReply reply = CommentDiscussionReply.builder()
                .parentComment(parentComment)
                .user(user)
                .content(content)
                .imageUrl(imageUrl)
                .build();

        CommentDiscussionReply savedReply = commentDiscussionReplyRepository.save(reply);
        return convertToReplyDTO(savedReply);
    }

    private CommentDiscussionReplyDTO convertToReplyDTO(CommentDiscussionReply reply) {
        CommentDiscussionReplyDTO dto = new CommentDiscussionReplyDTO();
        dto.setId(reply.getId());
        dto.setParentCommentId(reply.getParentComment().getCommentId());
        dto.setContent(reply.getContent());
        dto.setImageUrl(reply.getImageUrl());
        dto.setUser(userMapper.toDTO(reply.getUser()));
        dto.setCreatedAt(reply.getCreatedAt());
        return dto;
    }

    private CommentDTO convertToDTO(Comment comment, User currentUser) {
        CommentDTO dto = new CommentDTO();
        dto.setId(comment.getCommentId());
        dto.setContent(comment.getContent());
        dto.setImageUrl(comment.getImageUrl());
        dto.setDiscussionId(comment.getDiscussion().getDiscussionId());
        dto.setCreatedAt(comment.getCreatedAt());

        UserDTO userDTO = UserDTO.builder()
                .userId(comment.getUser().getUserId())
                .username(comment.getUser().getUsername())
                .fullName(comment.getUser().getFullName())
                .email(comment.getUser().getEmail())
                .urlAvatar(comment.getUser().getUrlAvatar())
                .role(comment.getUser().getRole())
                .createdAt(comment.getUser().getCreatedAt())
                .build();

        dto.setUser(userDTO);

        dto.setLikeCount((int) commentDiscussionLikeRepository.countByComment(comment));
        dto.setLikedByCurrentUser(
                currentUser != null &&
                        commentDiscussionLikeRepository.existsByCommentAndUser(comment, currentUser)
        );

        // Add replies
        List<CommentDiscussionReplyDTO> replies = commentDiscussionReplyRepository
                .findByParentCommentOrderByCreatedAtDesc(comment)
                .stream()
                .map(this::convertToReplyDTO)
                .collect(Collectors.toList());
        dto.setReplies(replies);
        return dto;
    }

    @Transactional
    public CommentDTO updateComment(Long commentId, String content, String imageUrl, Authentication authentication) {
        User currentUser = userService.getCurrentUser(authentication);
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new RuntimeException("Comment not found"));

        if (!comment.getUser().getUserId().equals(currentUser.getUserId())) {
            throw new RuntimeException("You don't have permission to edit this comment");
        }

        comment.setContent(content);
        if (imageUrl != null) {
            // Delete old image if exists
            if (comment.getImageUrl() != null) {
                s3Service.deleteFile(comment.getImageUrl());
            }
            comment.setImageUrl(imageUrl);
        }

        Comment updatedComment = commentRepository.save(comment);
        return convertToDTO(updatedComment, currentUser);
    }

    @Transactional
    public void deleteComment(Long commentId, Authentication authentication) {
        User currentUser = userService.getCurrentUser(authentication);
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new RuntimeException("Comment not found"));

        if (!comment.getUser().getUserId().equals(currentUser.getUserId()) &&
                !currentUser.getRole().equals("ROLE_ADMIN")) {
            throw new RuntimeException("You don't have permission to delete this comment");
        }

        // Delete associated likes
        commentDiscussionLikeRepository.deleteByComment(comment);

        // Delete associated replies
        commentDiscussionReplyRepository.deleteByParentComment(comment);

        // Delete image if exists
        if (comment.getImageUrl() != null) {
            s3Service.deleteFile(comment.getImageUrl());
        }

        commentRepository.delete(comment);
    }

    @Transactional
    public CommentDiscussionReplyDTO updateReply(Long replyId, String content, String imageUrl, Authentication authentication) {
        User currentUser = userService.getCurrentUser(authentication);
        CommentDiscussionReply reply = commentDiscussionReplyRepository.findById(replyId)
                .orElseThrow(() -> new RuntimeException("Reply not found"));

        if (!reply.getUser().getUserId().equals(currentUser.getUserId())) {
            throw new RuntimeException("You don't have permission to edit this reply");
        }

        reply.setContent(content);
        if (imageUrl != null) {
            // Delete old image if exists
            if (reply.getImageUrl() != null) {
                s3Service.deleteFile(reply.getImageUrl());
            }
            reply.setImageUrl(imageUrl);
        }

        CommentDiscussionReply updatedReply = commentDiscussionReplyRepository.save(reply);
        return convertToReplyDTO(updatedReply);
    }

    @Transactional
    public void deleteReply(Long replyId, Authentication authentication) {
        User currentUser = userService.getCurrentUser(authentication);
        CommentDiscussionReply reply = commentDiscussionReplyRepository.findById(replyId)
                .orElseThrow(() -> new RuntimeException("Reply not found"));

        if (!reply.getUser().getUserId().equals(currentUser.getUserId()) &&
                !currentUser.getRole().equals("ROLE_ADMIN")) {
            throw new RuntimeException("You don't have permission to delete this reply");
        }

        // Delete image if exists
        if (reply.getImageUrl() != null) {
            s3Service.deleteFile(reply.getImageUrl());
        }

        commentDiscussionReplyRepository.delete(reply);
    }

    public Long getForumIdByCommentId(Long commentId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new RuntimeException("Comment not found"));
        return comment.getDiscussion().getDiscussionId();
    }

    public Long getCommentIdByReplyId(Long replyId) {
        CommentDiscussionReply reply = commentDiscussionReplyRepository.findById(replyId)
                .orElseThrow(() -> new RuntimeException("Reply not found"));
        return reply.getParentComment().getCommentId();
    }

    public CommentDTO getCommentById(Long commentId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new RuntimeException("Comment not found"));
        return convertToDTO(comment, null); // Pass null as currentUser if not needed
    }
}