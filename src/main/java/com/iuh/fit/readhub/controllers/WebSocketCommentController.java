package com.iuh.fit.readhub.controllers;

import com.iuh.fit.readhub.dto.CommentDTO;
import com.iuh.fit.readhub.dto.CommentDiscussionReplyDTO;
import com.iuh.fit.readhub.dto.message.*;
import com.iuh.fit.readhub.services.CommentService;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Controller
@RequiredArgsConstructor
public class WebSocketCommentController {

    private final SimpMessagingTemplate messagingTemplate;
    private final CommentService commentService;

    @MessageMapping("/comment")
    public void handleComment(@Payload CommentMessage message,
                              SimpMessageHeaderAccessor headerAccessor) {
        try {
            Principal user = headerAccessor.getUser();
            if (user instanceof Authentication auth && auth.isAuthenticated()) {
                CommentDTO comment = commentService.createComment(message, auth);
                messagingTemplate.convertAndSend("/topic/forum/" + message.getDiscussionId(), comment);
            } else {
                messagingTemplate.convertAndSend("/topic/errors/" + message.getDiscussionId(),
                        Map.of("error", "User not authenticated", "discussionId", message.getDiscussionId()));
            }
        } catch (Exception e) {
            e.printStackTrace();
            messagingTemplate.convertAndSend("/topic/errors/" + message.getDiscussionId(),
                    Map.of(
                            "error", "Failed to process comment: " + e.getMessage(),
                            "discussionId", message.getDiscussionId()
                    )
            );
        }
    }

    @MessageMapping("/comment/like")
    public void handleLike(@Payload CommentDiscussionLikeMessage message,
                           SimpMessageHeaderAccessor headerAccessor) {
        try {
            Principal user = headerAccessor.getUser();
            if (user instanceof Authentication auth && auth.isAuthenticated()) {
                boolean isLiked = commentService.toggleLike(message.getCommentId(), auth);
                messagingTemplate.convertAndSend(
                        "/topic/comment/" + message.getCommentId() + "/like",
                        Map.of("liked", isLiked)
                );
            }
        } catch (Exception e) {
            handleError(message.getCommentId(), e);
        }
    }

    @MessageMapping("/comment/reply")
    public void handleReply(@Payload CommentDiscussionReplyMessage message,
                            SimpMessageHeaderAccessor headerAccessor) {
        try {
            Principal user = headerAccessor.getUser();
            if (user instanceof Authentication auth && auth.isAuthenticated()) {
                CommentDiscussionReplyDTO reply = commentService.createReply(
                        message.getCommentId(),
                        message.getContent(),
                        message.getImageUrl(),
                        auth
                );
                messagingTemplate.convertAndSend(
                        "/topic/comment/" + message.getCommentId() + "/reply",
                        reply
                );
            }
        } catch (Exception e) {
            handleError(message.getCommentId(), e);
        }
    }

    @MessageMapping("/comment/update")
    public void handleCommentUpdate(@Payload CommentDiscussionUpdateMessage message,
                                    SimpMessageHeaderAccessor headerAccessor) {
        try {
            Principal user = headerAccessor.getUser();
            if (user instanceof Authentication auth && auth.isAuthenticated()) {
                CommentDTO updatedComment = commentService.updateComment(
                        message.getCommentId(),
                        message.getContent(),
                        message.getImageUrl(),
                        auth
                );
                messagingTemplate.convertAndSend(
                        "/topic/comment/" + message.getCommentId() + "/update",
                        updatedComment
                );
            }
        } catch (Exception e) {
            handleError(message.getCommentId(), e, "update");
        }
    }

    @MessageMapping("/comment/delete")
    public void handleCommentDelete(@Payload CommentDiscussionDeleteMessage message,
                                    SimpMessageHeaderAccessor headerAccessor) {
        try {
            Principal user = headerAccessor.getUser();
            if (user instanceof Authentication auth && auth.isAuthenticated()) {
                commentService.deleteComment(message.getCommentId(), auth);
                messagingTemplate.convertAndSend(
                        "/topic/comment/" + message.getCommentId() + "/delete",
                        Map.of("commentId", message.getCommentId(), "deleted", true)
                );
            }
        } catch (Exception e) {
            handleError(message.getCommentId(), e, "delete");
        }
    }

    @MessageMapping("/comment/reply/update")
    public void handleReplyUpdate(@Payload CommentReplyDiscussionUpdateMessage message,
                                  SimpMessageHeaderAccessor headerAccessor) {
        try {
            Principal user = headerAccessor.getUser();
            if (user instanceof Authentication auth && auth.isAuthenticated()) {
                CommentDiscussionReplyDTO updatedReply = commentService.updateReply(
                        message.getReplyId(),
                        message.getContent(),
                        message.getImageUrl(),
                        auth
                );
                messagingTemplate.convertAndSend(
                        "/topic/reply/" + message.getReplyId() + "/update",
                        updatedReply
                );
            }
        } catch (Exception e) {
            handleError(message.getReplyId(), e, "reply-update");
        }
    }

    @MessageMapping("/comment/reply/delete")
    public void handleReplyDelete(@Payload CommentReplyDiscussionDeleteMessage message,
                                  SimpMessageHeaderAccessor headerAccessor) {
        try {
            Principal user = headerAccessor.getUser();
            if (user instanceof Authentication auth && auth.isAuthenticated()) {
                commentService.deleteReply(message.getReplyId(), auth);
                messagingTemplate.convertAndSend(
                        "/topic/reply/" + message.getReplyId() + "/delete",
                        Map.of("replyId", message.getReplyId(), "deleted", true)
                );
            }
        } catch (Exception e) {
            handleError(message.getReplyId(), e, "reply-delete");
        }
    }

    private void handleError(Long commentId, Exception e) {
        // Create error response
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("status", "error");
        errorResponse.put("message", e.getMessage());
        errorResponse.put("timestamp", LocalDateTime.now());
        errorResponse.put("commentId", commentId);
        // Send error message to specific error topic for this comment
        messagingTemplate.convertAndSend(
                "/topic/errors/comment/" + commentId,
                errorResponse
        );
        // Send error message to general error topic
        messagingTemplate.convertAndSend(
                "/topic/errors",
                errorResponse
        );
    }

    // Optional: You can create different types of error handling for different scenarios
    private void handleError(Long commentId, Exception e, String operation) {
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("status", "error");
        errorResponse.put("message", e.getMessage());
        errorResponse.put("timestamp", LocalDateTime.now());
        errorResponse.put("commentId", commentId);
        errorResponse.put("operation", operation); // like, reply, etc.

        String errorTopic = String.format("/topic/errors/comment/%d/%s", commentId, operation);
        messagingTemplate.convertAndSend(errorTopic, errorResponse);
    }
}