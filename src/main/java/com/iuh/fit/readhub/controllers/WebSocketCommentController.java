package com.iuh.fit.readhub.controllers;

import com.iuh.fit.readhub.dto.CommentDTO;
import com.iuh.fit.readhub.dto.CommentDiscussionReplyDTO;
import com.iuh.fit.readhub.dto.message.CommentDiscussionLikeMessage;
import com.iuh.fit.readhub.dto.message.CommentDiscussionReplyMessage;
import com.iuh.fit.readhub.dto.message.CommentMessage;
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

    private void handleError(Long commentId, Exception e) {
    }
}