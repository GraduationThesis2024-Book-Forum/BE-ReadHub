package com.iuh.fit.readhub.services;

import com.iuh.fit.readhub.dto.CommentDTO;
import com.iuh.fit.readhub.dto.UserDTO;
import com.iuh.fit.readhub.dto.message.CommentMessage;
import com.iuh.fit.readhub.mapper.UserMapper;
import com.iuh.fit.readhub.models.Comment;
import com.iuh.fit.readhub.models.Discussion;
import com.iuh.fit.readhub.models.User;
import com.iuh.fit.readhub.repositories.CommentRepository;
import com.iuh.fit.readhub.repositories.ForumRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
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

    @Transactional
    public CommentDTO createComment(CommentMessage message, Authentication authentication) {
        User user = userService.getCurrentUser(authentication);
        Discussion discussion = forumRepository.findById(message.getDiscussionId())
                .orElseThrow(() -> new RuntimeException("Forum not found"));

        Comment comment = Comment.builder()
                .content(message.getContent())
                .discussion(discussion)
                .user(user)
                .build();

        Comment savedComment = commentRepository.save(comment);
        return convertToDTO(savedComment);
    }

    public List<CommentDTO> getForumComments(Long forumId) {
        List<Comment> comments = commentRepository.findByDiscussion_DiscussionIdOrderByCreatedAtDesc(forumId);
        return comments.stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    private CommentDTO convertToDTO(Comment comment) {
        CommentDTO dto = new CommentDTO();
        dto.setId(comment.getCommentId());
        dto.setContent(comment.getContent());
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
        return dto;
    }

    private CommentDTO toDTO(Comment comment) {
        CommentDTO dto = new CommentDTO();
        dto.setId(comment.getCommentId());
        dto.setContent(comment.getContent());
        dto.setDiscussionId(comment.getDiscussion().getDiscussionId());
        dto.setUser(userMapper.toDTO(comment.getUser()));
        dto.setCreatedAt(comment.getCreatedAt());
        return dto;
    }
}