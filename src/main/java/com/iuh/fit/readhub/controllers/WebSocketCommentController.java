package com.iuh.fit.readhub.controllers;

import com.iuh.fit.readhub.dto.CommentDTO;
import com.iuh.fit.readhub.dto.message.CommentMessage;
import com.iuh.fit.readhub.services.CommentService;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
public class WebSocketCommentController {

    private final SimpMessagingTemplate messagingTemplate;
    private final CommentService commentService;

    @MessageMapping("/comment")
    public void handleComment(@Payload CommentMessage message, Authentication authentication) {
        CommentDTO comment = commentService.createComment(message, authentication);
        messagingTemplate.convertAndSend("/topic/forum/" + message.getDiscussionId(), comment);
    }
}