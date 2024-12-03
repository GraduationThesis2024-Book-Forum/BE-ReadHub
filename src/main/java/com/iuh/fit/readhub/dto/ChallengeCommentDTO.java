package com.iuh.fit.readhub.dto;

import com.iuh.fit.readhub.models.Book;
import com.iuh.fit.readhub.models.ChallengeComment;
import com.iuh.fit.readhub.models.User;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class ChallengeCommentDTO {
    private Long id;
    private String content;
    private UserDTO user;
    private List<BookDTO> books;
    private LocalDateTime createdAt;
    private boolean isOwner;

    private ChallengeCommentDTO convertToDTO(ChallengeComment comment, User currentUser) {
        List<Book> books = comment.getBookIds().stream()
                .map(bookRepository::getById)
                .collect(Collectors.toList());

        return ChallengeCommentDTO.builder()
                .id(comment.getId())
                .content(comment.getContent())
                .user(userMapper.toDTO(comment.getUser()))
                .books(books.stream()
                        .map(book -> new BookDTO(
                                book.getId(),
                                book.getTitle(),
                                book.getAuthor(),
                                book.getCoverUrl()
                        ))
                        .collect(Collectors.toList()))
                .createdAt(comment.getCreatedAt())
                .isOwner(comment.getUser().equals(currentUser))
                .build();
    }
}
