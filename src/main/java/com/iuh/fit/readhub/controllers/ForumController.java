package com.iuh.fit.readhub.controllers;

import com.iuh.fit.readhub.dto.ApiResponse;
import com.iuh.fit.readhub.dto.request.ForumRequest;
import com.iuh.fit.readhub.models.Forum;
import com.iuh.fit.readhub.models.User;
import com.iuh.fit.readhub.services.ForumService;
import com.iuh.fit.readhub.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("api/v1/forums")
public class ForumController {

    @Autowired
    private ForumService forumService;

    @Autowired
    private UserService userService;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('ROLE_ADMIN') || hasRole('ROLE_USER')")
    public ResponseEntity<ApiResponse<?>> createForum(
            @ModelAttribute ForumRequest request,
            Authentication authentication) {
        try {
            User creator = userService.getCurrentUser(authentication);
            Forum createdForum = forumService.createForum(request, creator);

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
}
