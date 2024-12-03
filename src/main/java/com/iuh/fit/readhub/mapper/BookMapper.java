package com.iuh.fit.readhub.mapper;

import com.iuh.fit.readhub.dto.BookDTO;
import com.iuh.fit.readhub.models.Book;

public class BookMapper {
    public BookDTO toDTO(Book book) {
        return BookDTO.builder()
                .id(book.getBookId())
                .title(book.getTitle())
                .author(book.getBookAuthors())
                .coverUrl(book.getCoverUrl())
                .build();
    }
}