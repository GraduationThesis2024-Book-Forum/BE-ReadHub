package com.iuh.fit.readhub.controllers;

import com.iuh.fit.readhub.constants.NotificationType;
import com.iuh.fit.readhub.dto.ApiResponse;
import com.iuh.fit.readhub.dto.CommentDTO;
import com.iuh.fit.readhub.dto.ForumDTO;
import com.iuh.fit.readhub.dto.ForumInteractionDTO;
import com.iuh.fit.readhub.dto.request.ForumReportRequest;
import com.iuh.fit.readhub.dto.request.ForumRequest;
import com.iuh.fit.readhub.dto.request.ReportActionRequest;
import com.iuh.fit.readhub.models.ForumReport;
import com.iuh.fit.readhub.models.User;
import com.iuh.fit.readhub.services.CommentService;
import com.iuh.fit.readhub.services.FCMService;
import com.iuh.fit.readhub.services.ForumService;
import com.iuh.fit.readhub.services.UserService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("api/v1/forums")
public class ForumController {

    private final ForumService forumService;

    private final UserService userService;

    private final CommentService commentService;

    private final FCMService fcmService;

    public ForumController(
            ForumService forumService,
            UserService userService,
            CommentService commentService, FCMService fcmService) {
        this.forumService = forumService;
        this.userService = userService;
        this.commentService = commentService;
        this.fcmService = fcmService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<?>> getAllForums() {
        try {
            List<ForumDTO> forums = forumService.getAllForums();
            return ResponseEntity.ok(ApiResponse.builder()
                    .message("Lấy danh sách diễn đàn thành công")
                    .status(200)
                    .data(forums)
                    .success(true)
                    .build());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.builder()
                    .message("Lỗi khi lấy danh sách diễn đàn: " + e.getMessage())
                    .status(400)
                    .success(false)
                    .build());
        }
    }

    @PostMapping("/reports/{reportId}/action")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<ApiResponse<?>> handleReportAction(
            @PathVariable Long reportId,
            @RequestBody ReportActionRequest request,
            Authentication authentication) {
        try {
            ForumReport report = forumService.handleReportAction(reportId, request);

            Map<String, String> data = Map.of(
                    "type", NotificationType.REPORT_ACTION.name(),
                    "reportId", reportId.toString(),
                    "action", request.getAction().name(),
                    "forumId", report.getForum().getDiscussionId().toString()
            );

            fcmService.sendNotification(
                    report.getForum().getCreator().getUserId(),
                    NotificationType.REPORT_ACTION.getTitle(),
                    request.getAction().getNotificationMessage(),
                    data
            );

            return ResponseEntity.ok(ApiResponse.builder()
                    .message("Report action applied successfully")
                    .status(200)
                    .success(true)
                    .build());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.builder()
                    .message("Error applying report action: " + e.getMessage())
                    .status(400)
                    .success(false)
                    .build());
        }
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('ROLE_USER') or hasRole('ROLE_ADMIN')")
    public ResponseEntity<ApiResponse<?>> createForum(
            @ModelAttribute ForumRequest request,
            Authentication authentication) {
        try {
            User creator = userService.getCurrentUser(authentication);
            ForumDTO createdForum = forumService.createForum(request, creator);

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
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<?>> joinForum(
            @PathVariable Long forumId,
            Authentication authentication) {
        try {
            User user = userService.getCurrentUser(authentication);
            ForumDTO forum = forumService.joinForum(forumId, user);

            // Notify forum creator
            Map<String, String> data = new HashMap<>();
            data.put("type", "NEW_MEMBER");
            data.put("forumId", forumId.toString());
            data.put("memberId", user.getUserId().toString());

            fcmService.sendNotification(
                    forum.getCreator().getUserId(),
                    "New Forum Member",
                    user.getUsername() + " has joined your forum",
                    data
            );

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

    @GetMapping("/{forumId}/membership")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<?>> checkMembership(
            @PathVariable Long forumId,
            Authentication authentication) {
        try {
            User user = userService.getCurrentUser(authentication);
            boolean isMember = forumService.isForumMember(forumId, user.getUserId());

            return ResponseEntity.ok(ApiResponse.builder()
                    .message("Kiểm tra thành viên thành công")
                    .status(200)
                    .data(isMember)
                    .success(true)
                    .build());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.builder()
                    .message("Lỗi khi kiểm tra thành viên: " + e.getMessage())
                    .status(400)
                    .success(false)
                    .build());
        }
    }

    @GetMapping("/{forumId}")
    public ResponseEntity<ApiResponse<?>> getForumById(@PathVariable Long forumId) {
        try {
            ForumDTO forum = forumService.getForumById(forumId);
            return ResponseEntity.ok(ApiResponse.builder()
                    .message("Lấy thông tin diễn đàn thành công")
                    .status(200)
                    .data(forum)
                    .success(true)
                    .build());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.builder()
                    .message("Lỗi khi lấy thông tin diễn đàn: " + e.getMessage())
                    .status(400)
                    .success(false)
                    .build());
        }
    }

    @PostMapping("/{forumId}/like")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<?>> toggleLike(
            @PathVariable Long forumId,
            Authentication authentication) {
        try {
            User user = userService.getCurrentUser(authentication);
            ForumInteractionDTO result = forumService.toggleLike(forumId, user);
            return ResponseEntity.ok(ApiResponse.builder()
                    .message("Cập nhật like thành công")
                    .status(200)
                    .data(result)
                    .success(true)
                    .build());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.builder()
                    .message("Lỗi khi cập nhật like: " + e.getMessage())
                    .status(400)
                    .success(false)
                    .build());
        }
    }

    @PostMapping("/{forumId}/save")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<?>> toggleSave(
            @PathVariable Long forumId,
            Authentication authentication) {
        try {
            User user = userService.getCurrentUser(authentication);
            ForumInteractionDTO result = forumService.toggleSave(forumId, user);
            return ResponseEntity.ok(ApiResponse.builder()
                    .message("Cập nhật save thành công")
                    .status(200)
                    .data(result)
                    .success(true)
                    .build());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.builder()
                    .message("Lỗi khi cập nhật save: " + e.getMessage())
                    .status(400)
                    .success(false)
                    .build());
        }
    }

    @GetMapping("/{forumId}/interactions")
    public ResponseEntity<ApiResponse<?>> getInteractions(
            @PathVariable Long forumId,
            Authentication authentication) {
        try {
            User user = userService.getCurrentUser(authentication);
            ForumInteractionDTO interactions = forumService.getForumInteractions(forumId, user);
            return ResponseEntity.ok(ApiResponse.builder()
                    .message("Lấy thông tin tương tác thành công")
                    .status(200)
                    .data(interactions)
                    .success(true)
                    .build());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.builder()
                    .message("Lỗi khi lấy thông tin tương tác: " + e.getMessage())
                    .status(400)
                    .success(false)
                    .build());
        }
    }

    @DeleteMapping("/{forumId}")
    @PreAuthorize("hasRole('ROLE_ADMIN') or @forumService.isForumCreator(#forumId, authentication)")
    public ResponseEntity<ApiResponse<?>> deleteForum(
            @PathVariable Long forumId,
            Authentication authentication) {
        try {
            forumService.deleteForum(forumId);
            return ResponseEntity.ok(ApiResponse.builder()
                    .message("Xóa diễn đàn thành công")
                    .status(200)
                    .success(true)
                    .build());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.builder()
                    .message("Lỗi khi xóa diễn đàn: " + e.getMessage())
                    .status(400)
                    .success(false)
                    .build());
        }
    }

    @PostMapping("/{forumId}/report")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<?>> reportForum(
            @PathVariable Long forumId,
            @RequestBody ForumReportRequest request,
            Authentication authentication) {
        try {
            User reporter = userService.getCurrentUser(authentication);
            forumService.reportForum(forumId, reporter, request.getReason(), request.getAdditionalInfo());

            return ResponseEntity.ok(ApiResponse.builder()
                    .message("Forum reported successfully")
                    .status(200)
                    .success(true)
                    .build());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.builder()
                    .message("Error reporting forum: " + e.getMessage())
                    .status(400)
                    .success(false)
                    .build());
        }
    }
}
