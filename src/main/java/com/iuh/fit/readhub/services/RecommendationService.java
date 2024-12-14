package com.iuh.fit.readhub.services;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.iuh.fit.readhub.SavedBookRepository;
import com.iuh.fit.readhub.dto.BookListResponse;
import com.iuh.fit.readhub.dto.request.BookSearchCriteria;
import com.iuh.fit.readhub.models.*;
import com.iuh.fit.readhub.repositories.ReadingHistoryRepository;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Service
@AllArgsConstructor
public class RecommendationService {
    private static final int MAX_RECOMMENDATIONS = 20;

    @Autowired
    private SavedBookRepository savedBookRepository;

    @Autowired
    private ReadingHistoryRepository readingHistoryRepository;

    @Autowired
    private BookService bookService;

    @Cacheable(value = "recommendations", key = "#userId")
    public List<Long> getRecommendedBookIds(Long userId) {
        try {
            List<Long> userBookIds = getUserBooks(userId);
            if (userBookIds.isEmpty()) {
                return getTopRatedBooks();
            }

            // Lấy 3 cuốn sách gần nhất
            List<Long> recentBooks = userBookIds.subList(0, Math.min(3, userBookIds.size()));

            Map<String, Integer> subjectFrequencyMap = new ConcurrentHashMap<>();
            Map<String, Integer> authorFrequencyMap = new ConcurrentHashMap<>();

            for (Long bookId : recentBooks) {
                Optional<Book> bookOpt = bookService.getBookById(bookId);
                if (bookOpt.isPresent()) {
                    Book book = bookOpt.get();
                    analyzeBookDetails(book, subjectFrequencyMap, authorFrequencyMap);
                }
            }

            // Lấy top subjects và authors
            List<String> topSubjects = getTopEntries(subjectFrequencyMap, 2);
            List<String> topAuthors = getTopEntries(authorFrequencyMap, 2);

            Set<Long> recommendedBooks = new HashSet<>();
            Set<Long> userBooksSet = new HashSet<>(userBookIds);

            // Tìm sách theo subjects
            for (String subject : topSubjects) {
                findBooksBySubject(subject, recommendedBooks, userBooksSet);
            }

            // Tìm sách theo authors
            for (String author : topAuthors) {
                findBooksByAuthor(author, recommendedBooks, userBooksSet);
            }

            // Thêm top rated books nếu chưa đủ
            if (recommendedBooks.size() < MAX_RECOMMENDATIONS) {
                addTopRatedBooks(recommendedBooks, userBooksSet);
            }

            return new ArrayList<>(recommendedBooks)
                    .stream()
                    .limit(MAX_RECOMMENDATIONS)
                    .collect(Collectors.toList());

        } catch (Exception e) {
            e.printStackTrace();
            return getTopRatedBooks();
        }
    }

    private void analyzeBookDetails(Book book, Map<String, Integer> subjectFrequencyMap, Map<String, Integer> authorFrequencyMap) {
        // Phân tích subjects
        if (book.getSubjects() != null) {
            for (String subject : book.getSubjects()) {
                String mainSubject = subject.split("--")[0].trim();
                if (!mainSubject.equals("Fiction")) {
                    subjectFrequencyMap.merge(mainSubject, 1, Integer::sum);
                }
            }
        }

        // Phân tích authors
        if (book.getAuthors() != null) {
            for (Author author : book.getAuthors()) {
                authorFrequencyMap.merge(author.getName(), 1, Integer::sum);
            }
        }
    }

    private List<String> getTopEntries(Map<String, Integer> map, int limit) {
        return map.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .limit(limit)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
    }

    private List<Long> getUserBooks(Long userId) {
        Set<Long> userBooks = new LinkedHashSet<>();
        readingHistoryRepository.findByUser_UserId(userId)
                .forEach(history -> userBooks.add(history.getBookId()));
        savedBookRepository.findByUser_UserIdOrderBySavedAtDesc(userId)
                .forEach(saved -> userBooks.add(saved.getBookId()));
        return new ArrayList<>(userBooks);
    }

    private void findBooksBySubject(String subject, Set<Long> recommendedBooks, Set<Long> userBooks) {
        BookSearchCriteria criteria = new BookSearchCriteria();
        criteria.setSubjects(Collections.singletonList(subject));
        BookListResponse response = bookService.searchBooks(criteria, 0, 20);
        response.getResults().stream()
                .map(Book::getId)
                .filter(id -> !userBooks.contains(id))
                .forEach(recommendedBooks::add);
    }

    private void findBooksByAuthor(String author, Set<Long> recommendedBooks, Set<Long> userBooks) {
        BookSearchCriteria criteria = new BookSearchCriteria();
        criteria.setAuthor(author);
        BookListResponse response = bookService.searchBooks(criteria, 0, 20);
        response.getResults().stream()
                .map(Book::getId)
                .filter(id -> !userBooks.contains(id))
                .forEach(recommendedBooks::add);
    }

    private List<Long> getTopRatedBooks() {
        BookListResponse response = bookService.getTop20Book();
        return response.getResults().stream()
                .map(Book::getId)
                .collect(Collectors.toList());
    }

    private void addTopRatedBooks(Set<Long> recommendedBooks, Set<Long> userBooks) {
        getTopRatedBooks().stream()
                .filter(id -> !userBooks.contains(id))
                .forEach(recommendedBooks::add);
    }
}