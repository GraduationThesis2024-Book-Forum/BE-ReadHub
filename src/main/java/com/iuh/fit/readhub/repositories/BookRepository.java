package com.iuh.fit.readhub.repositories;

import com.iuh.fit.readhub.dto.request.BookSearchCriteria;
import com.iuh.fit.readhub.models.Book;
import org.bson.types.ObjectId;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BookRepository extends MongoRepository<Book, Long> {

    Optional<Book> findById(Long id);

    // Search by title with pagination
    Page<Book> findByTitleContainingIgnoreCase(String title, Pageable pageable);

    // Advanced search with multiple criteria
    @Query("{ $and: [ " +
            "?#{ [0].title == null ? { $expr: true } : { 'title': { $regex: [0].title, $options: 'i' } } }, " +
            "?#{ [0].author == null ? { $expr: true } : { 'authors.name': { $regex: [0].author, $options: 'i' } } }, " +
            "?#{ [0].languages == null ? { $expr: true } : { 'languages': { $in: [0].languages } } }, " +
            "?#{ [0].subjects == null ? { $expr: true } : { 'subjects': { $in: [0].subjects } } }, " +
            "?#{ [0].bookshelves == null ? { $expr: true } : { 'bookshelves': { $in: [0].bookshelves } } } " +
            "] }")
    Page<Book> searchBooks(BookSearchCriteria criteria, Pageable pageable);

    // Find multiple books by list of ids
    List<Book> findByIdIn(List<Long> ids);

}