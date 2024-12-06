package com.iuh.fit.readhub.controllers;

import com.iuh.fit.readhub.dto.ChallengeCommentDTO;
import com.iuh.fit.readhub.dto.message.ChallengeCommentDeleteMessage;
import com.iuh.fit.readhub.dto.message.ChallengeCommentMessage;
import com.iuh.fit.readhub.services.ChallengeCommentService;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;

import java.security.Principal;
import java.util.Map;

@Controller
@RequiredArgsConstructor
public class ChallengeCommentWebSocketController {
    private final SimpMessagingTemplate messagingTemplate;
    private final ChallengeCommentService commentService;

    @MessageMapping("/challenge/comment")
    public void handleComment(@Payload ChallengeCommentMessage message,
                              SimpMessageHeaderAccessor headerAccessor) {
        try {
            Principal user = headerAccessor.getUser();
            if (user instanceof Authentication auth && auth.isAuthenticated()) {
                ChallengeCommentDTO comment = commentService.createComment(message, auth);
                messagingTemplate.convertAndSend("/topic/challenge/" + message.getChallengeId(), comment);
            }
        } catch (Exception e) {
            messagingTemplate.convertAndSend("/topic/errors/challenge/" + message.getChallengeId(),
                    Map.of("error", "Failed to process comment: " + e.getMessage()));
        }
    }

    @MessageMapping("/challenge/comment/delete")
    public void handleDelete(@Payload ChallengeCommentDeleteMessage message,
                             SimpMessageHeaderAccessor headerAccessor) {
        try {
            Principal user = headerAccessor.getUser();
            if (user instanceof Authentication auth && auth.isAuthenticated()) {
                commentService.deleteComment(message.getCommentId(), auth);
                messagingTemplate.convertAndSend(
                        "/topic/challenge/" + message.getChallengeId() + "/comment-delete",
                        Map.of("commentId", message.getCommentId(), "deleted", true)
                );
            }
        } catch (Exception e) {
            messagingTemplate.convertAndSend("/topic/errors/challenge/" + message.getChallengeId(),
                    Map.of("error", "Failed to delete comment: " + e.getMessage()));
        }
    }
}