package com.iuh.fit.readhub.services;

import com.iuh.fit.readhub.dto.BookDTO;
import com.iuh.fit.readhub.dto.ChallengeCommentDTO;
import com.iuh.fit.readhub.dto.message.ChallengeCommentMessage;
import com.iuh.fit.readhub.mapper.UserMapper;
import com.iuh.fit.readhub.models.Book;
import com.iuh.fit.readhub.models.ChallengeComment;
import com.iuh.fit.readhub.models.ForumChallenge;
import com.iuh.fit.readhub.models.User;
import com.iuh.fit.readhub.repositories.BookRepository;
import com.iuh.fit.readhub.repositories.ChallengeCommentRepository;
import com.iuh.fit.readhub.repositories.ForumChallengeRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.nio.file.AccessDeniedException;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ChallengeCommentService {
    private final ChallengeCommentRepository commentRepository;
    private final UserService userService;
    private final ForumChallengeRepository challengeRepository;
    private final UserMapper userMapper;
    private final BookRepository bookRepository;

    public ChallengeCommentDTO createComment(ChallengeCommentMessage message, Authentication auth) {
        User user = userService.getCurrentUser(auth);
        ForumChallenge challenge = challengeRepository.findById(message.getChallengeId())
                .orElseThrow(() -> new EntityNotFoundException("Challenge not found"));

        ChallengeComment comment = ChallengeComment.builder()
                .content(message.getContent())
                .user(user)
                .challenge(challenge)
                .bookIds(message.getBookIds())
                .build();

        return convertToDTO(commentRepository.save(comment));
    }

    public void deleteComment(Long commentId, Authentication auth) throws AccessDeniedException {
        ChallengeComment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new EntityNotFoundException("Comment not found"));

        User user = userService.getCurrentUser(auth);
        if (!comment.getUser().equals(user) && !user.getRole().equals("ROLE_ADMIN")) {
            throw new AccessDeniedException("Cannot delete other's comments");
        }

        commentRepository.delete(comment);
    }

    private ChallengeCommentDTO convertToDTO(ChallengeComment comment, User currentUser) {
        List<BookDTO> bookDtos = comment.getBookIds().stream()
                .map(bookId -> {
                    Book book = bookRepository.getById(bookId);
                    return new BookDTO(
                            book.getBookId(),
                            book.getTitle(),
                            book.getBookAuthors(),
                            book.getCoverImage()
                    );
                })
                .collect(Collectors.toList());

        return ChallengeCommentDTO.builder()
                .id(comment.getId())
                .content(comment.getContent())
                .user(userMapper.toDTO(comment.getUser()))
                .books(bookDtos)
                .createdAt(comment.getCreatedAt())
                .isOwner(comment.getUser().equals(currentUser))
                .build();
    }
}