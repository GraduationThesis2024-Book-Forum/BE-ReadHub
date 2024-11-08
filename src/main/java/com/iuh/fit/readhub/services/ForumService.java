package com.iuh.fit.readhub.services;

import com.iuh.fit.readhub.dto.ForumDTO;
import com.iuh.fit.readhub.dto.request.ForumRequest;
import com.iuh.fit.readhub.exceptions.ForumException;
import com.iuh.fit.readhub.mapper.UserMapper;
import com.iuh.fit.readhub.models.Discussion;
import com.iuh.fit.readhub.models.ForumMember;
import com.iuh.fit.readhub.models.User;
import com.iuh.fit.readhub.repositories.ForumMemberRepository;
import com.iuh.fit.readhub.repositories.ForumRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class ForumService {

    private final ForumRepository forumRepository;
    private final ForumMemberRepository forumMemberRepository;
    private final UserMapper userMapper;
    private final S3Service s3Service;


    public ForumService(ForumRepository forumRepository,
                        ForumMemberRepository forumMemberRepository,
                        UserMapper userMapper,
                        S3Service s3Service) {
        this.forumRepository = forumRepository;
        this.forumMemberRepository = forumMemberRepository;
        this.userMapper = userMapper;
        this.s3Service = s3Service;
    }

    public Discussion createForum(ForumRequest request, User creator) {
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

        return forumRepository.save(forum);
    }

    // Thêm method để update forum image nếu cần
    public void updateForumImage(Long forumId, MultipartFile newImage) {
        Discussion forum = forumRepository.findById(forumId)
                .orElseThrow(() -> new ForumException("Diễn đàn không tồn tại"));
        if (forum.getImageUrl() != null) {
            s3Service.deleteFile(forum.getImageUrl());
        }
        String newImageUrl = s3Service.uploadFile(newImage);
        forum.setImageUrl(newImageUrl);
        forumRepository.save(forum);
    }

    @Transactional
    public Discussion joinForum(Long forumId, User user) {
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

        // Refresh discussion để lấy danh sách members mới nhất
        forumRepository.refreshAndLock(forumId);

        return discussion;
    }

    public List<User> getForumMembers(Long forumId) {
        Discussion discussion = forumRepository.findById(forumId)
                .orElseThrow(() -> new ForumException("Diễn đàn không tồn tại"));

        return discussion.getMembers().stream()
                .map(ForumMember::getUser)
                .collect(Collectors.toList());
    }

    // Thêm method để kiểm tra user có phải là thành viên
    public boolean isForumMember(Long forumId, Long userId) {
        return forumMemberRepository.existsByDiscussion_DiscussionIdAndUser_UserId(forumId, userId);
    }

    public List<ForumDTO> getAllForums() {
        List<Discussion> forums = forumRepository.findAll();
        return forums.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    private ForumDTO convertToDTO(Discussion discussion) {
        return ForumDTO.builder()
                .discussionId(discussion.getDiscussionId())
                .forumTitle(discussion.getForumTitle())
                .forumDescription(discussion.getForumDescription())
                .imageUrl(discussion.getImageUrl())
                .bookTitle(discussion.getBookTitle())
                .authors(discussion.getAuthors())
                .subjects(discussion.getSubjects())
                .categories(discussion.getCategories())
                .creator(userMapper.toDTO(discussion.getCreator()))
                .totalMembers(discussion.getMembers().size())
                .totalPosts(discussion.getComments().size())
                .createdAt(discussion.getCreatedAt())
                .updatedAt(discussion.getUpdatedAt())
                .trending(discussion.getComments().size() > 10) // Example logic for trending
                .build();
    }
}