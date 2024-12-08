package com.iuh.fit.readhub.services;

import com.iuh.fit.readhub.dto.BookListResponse;
import com.iuh.fit.readhub.dto.request.BookSearchCriteria;
import com.iuh.fit.readhub.models.Book;
import com.iuh.fit.readhub.repositories.BookRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@AllArgsConstructor
@Slf4j
public class BookService {

    @Autowired
    private BookRepository bookRepository;

    public Optional<Book> getBookById(Long id) {
        return bookRepository.findById(id);
    }

    public BookListResponse searchByTitle(String title, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Book> bookPage = bookRepository.findByTitleContainingIgnoreCase(title, pageable);
        return createBookListResponse(bookPage);
    }

    public BookListResponse searchBooks(BookSearchCriteria criteria, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Book> bookPage = bookRepository.searchBooks(criteria, pageable);
        return createBookListResponse(bookPage);
    }

    public BookListResponse getBooksByIds(List<Long> ids, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        List<Book> books = bookRepository.findByIdIn(ids);

        int start = (int)pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), books.size());

        Page<Book> bookPage = new PageImpl<>(
                books.subList(start, end),
                pageable,
                books.size()
        );

        return createBookListResponse(bookPage);
    }

    private BookListResponse createBookListResponse(Page<Book> bookPage) {
        String nextPage = bookPage.hasNext() ?
                "/books?page=" + (bookPage.getNumber() + 1) : null;

        String previousPage = bookPage.hasPrevious() ?
                "/books?page=" + (bookPage.getNumber() - 1) : null;

        return BookListResponse.builder()
                .count(bookPage.getTotalElements())
                .next(nextPage)
                .previous(previousPage)
                .results(bookPage.getContent())
                .build();
    }

    public void updateAverageRating(Long bookId, double averageRating) {
        Optional<Book> book = bookRepository.findById(bookId);
        if (book.isPresent()) {
            book.get().setAverageRating(averageRating);
            bookRepository.save(book.get());
        }
    }

    public BookListResponse getTop20Book() {
        List<Book> books = bookRepository.findAll();
        books.sort(Comparator.comparing(Book::getAverageRating).reversed());
        List<Book> top20Books = books.subList(0, Math.min(20, books.size()));
        return BookListResponse.builder()
                .count((long) top20Books.size())
                .results(top20Books)
                .build();
    }
}

