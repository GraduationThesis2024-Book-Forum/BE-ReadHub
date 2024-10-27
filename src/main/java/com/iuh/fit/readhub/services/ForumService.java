package com.iuh.fit.readhub.services;

import com.iuh.fit.readhub.dto.request.ForumRequest;
import com.iuh.fit.readhub.models.Forum;
import com.iuh.fit.readhub.models.User;
import com.iuh.fit.readhub.repositories.ForumRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Service
public class ForumService {

    @Autowired
    private ForumRepository forumRepository;

    @Value("${app.upload.dir:uploads/forums}")
    private String uploadDir;

    public Forum createForum(ForumRequest request, User creator) throws IOException {
        String imageUrl = null;
        if (request.getForumImage() != null && !request.getForumImage().isEmpty()) {
            imageUrl = saveImage(request.getForumImage());
        }

        Forum forum = Forum.builder()
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
}