package com.iuh.fit.readhub.controllers;

import com.iuh.fit.readhub.dto.ApiResponse;
import com.iuh.fit.readhub.dto.CommentDTO;
import com.iuh.fit.readhub.dto.request.ForumRequest;
import com.iuh.fit.readhub.models.Discussion;
import com.iuh.fit.readhub.models.User;
import com.iuh.fit.readhub.services.CommentService;
import com.iuh.fit.readhub.services.ForumService;
import com.iuh.fit.readhub.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/v1/forums")
public class ForumController {

    @Autowired
    private ForumService forumService;

    @Autowired
    private UserService userService;
    @Autowired
    private CommentService commentService;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('ROLE_ADMIN') || hasRole('ROLE_USER')")
    public ResponseEntity<ApiResponse<?>> createForum(
            @ModelAttribute ForumRequest request,
            Authentication authentication) {
        try {
            User creator = userService.getCurrentUser(authentication);
            Discussion createdForum = forumService.createForum(request, creator);

            return ResponseEntity.ok(ApiResponse.builder()
                    .message("Tạo diễn đàn thành công")
                    .status(200)
                    .data(createdForum)
                    .success(true)
                    .build());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.builder()
                    .message("Lỗi khi tạo diễn đàn: " + e.getMessage())
                    .status(400)
                    .success(false)
                    .build());
        }
    }

    @GetMapping("/{forumId}/comments")
    public ResponseEntity<ApiResponse<?>> getForumComments(@PathVariable Long forumId) {
        try {
            List<CommentDTO> comments = commentService.getForumComments(forumId);
            return ResponseEntity.ok(ApiResponse.builder()
                    .message("Lấy danh sách bình luận thành công")
                    .status(200)
                    .data(comments)
                    .success(true)
                    .build());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.builder()
                    .message("Lỗi khi lấy bình luận: " + e.getMessage())
                    .status(400)
                    .success(false)
                    .build());
        }
    }

    @PostMapping("/{forumId}/join")
    @PreAuthorize("hasRole('ROLE_USER')")
    public ResponseEntity<ApiResponse<?>> joinForum(
            @PathVariable Long forumId,
            Authentication authentication) {
        try {
            User user = userService.getCurrentUser(authentication);
            Discussion forum = forumService.joinForum(forumId, user);

            return ResponseEntity.ok(ApiResponse.builder()
                    .message("Tham gia diễn đàn thành công")
                    .status(200)
                    .data(forum)
                    .success(true)
                    .build());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.builder()
                    .message("Lỗi khi tham gia diễn đàn: " + e.getMessage())
                    .status(400)
                    .success(false)
                    .build());
        }
    }
}
