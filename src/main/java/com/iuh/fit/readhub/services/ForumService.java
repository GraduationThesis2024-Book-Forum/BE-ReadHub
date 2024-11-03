package com.iuh.fit.readhub.services;

import com.iuh.fit.readhub.dto.request.ForumRequest;
import com.iuh.fit.readhub.exceptions.ForumException;
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

    @Value("${app.upload.dir:uploads/forums}")
    private String uploadDir;

    public ForumService(ForumRepository forumRepository, ForumMemberRepository forumMemberRepository) {
        this.forumRepository = forumRepository;
        this.forumMemberRepository = forumMemberRepository;
    }

    public Discussion createForum(ForumRequest request, User creator) throws IOException {
        String imageUrl = null;
        if (request.getForumImage() != null && !request.getForumImage().isEmpty()) {
            imageUrl = saveImage(request.getForumImage());
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

    private String saveImage(MultipartFile file) throws IOException {
        // Create upload directory if it doesn't exist
        Path uploadPath = Paths.get(uploadDir);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        // Generate unique filename
        String filename = UUID.randomUUID().toString() + "_" + file.getOriginalFilename();
        Path filePath = uploadPath.resolve(filename);

        // Save file
        Files.copy(file.getInputStream(), filePath);

        return filename;
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
}