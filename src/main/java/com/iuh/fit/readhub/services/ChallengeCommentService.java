package com.iuh.fit.readhub.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.iuh.fit.readhub.dto.ChallengeCommentDTO;
import com.iuh.fit.readhub.dto.GutendexBookDTO;
import com.iuh.fit.readhub.dto.message.ChallengeCommentMessage;
import com.iuh.fit.readhub.mapper.UserMapper;
import com.iuh.fit.readhub.models.ChallengeComment;
import com.iuh.fit.readhub.models.ForumChallenge;
import com.iuh.fit.readhub.models.User;
import com.iuh.fit.readhub.repositories.ChallengeCommentRepository;
import com.iuh.fit.readhub.repositories.ForumChallengeRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.nio.file.AccessDeniedException;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ChallengeCommentService {
    private final ChallengeCommentRepository commentRepository;
    private final UserService userService;
    private final ForumChallengeRepository challengeRepository;
    private final UserMapper userMapper;
    private final ObjectMapper objectMapper;  // For JSON conversion

    public ChallengeCommentDTO createComment(ChallengeCommentMessage message, Authentication auth) {
        User currentUser = userService.getCurrentUser(auth);
        ForumChallenge challenge = challengeRepository.findById(message.getChallengeId())
                .orElseThrow(() -> new EntityNotFoundException("Challenge not found"));

        try {
            // Convert books to JSON string
            String booksJson = objectMapper.writeValueAsString(message.getBooks());

            ChallengeComment comment = ChallengeComment.builder()
                    .content(message.getContent())
                    .imageUrl(message.getImageUrl())
                    .user(currentUser)
                    .challenge(challenge)
                    .booksJson(booksJson)
                    .build();

            ChallengeComment savedComment = commentRepository.save(comment);
            return convertToDTO(savedComment, currentUser);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Error processing book information", e);
        }
    }

    public void deleteComment(Long commentId, Authentication auth) throws AccessDeniedException {
        User currentUser = userService.getCurrentUser(auth);
        ChallengeComment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new EntityNotFoundException("Comment not found"));

        if (!comment.getUser().equals(currentUser) && !currentUser.getRole().equals("ROLE_ADMIN")) {
            throw new AccessDeniedException("Cannot delete other's comments");
        }

        commentRepository.delete(comment);
    }

    private ChallengeCommentDTO convertToDTO(ChallengeComment comment, User currentUser) {
        List<GutendexBookDTO> books;
        try {
            // Convert JSON string back to list of books
            books = objectMapper.readValue(
                    comment.getBooksJson(),
                    objectMapper.getTypeFactory().constructCollectionType(List.class, GutendexBookDTO.class)
            );
        } catch (JsonProcessingException e) {
            books = new ArrayList<>();  // Empty list if there's an error
        }

        return ChallengeCommentDTO.builder()
                .id(comment.getId())
                .content(comment.getContent())
                .imageUrl(comment.getImageUrl())
                .user(userMapper.toDTO(comment.getUser()))
                .books(books)
                .createdAt(comment.getCreatedAt())
                .isOwner(comment.getUser().equals(currentUser))
                .build();
    }
}