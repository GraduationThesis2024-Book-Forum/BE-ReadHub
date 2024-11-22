package com.iuh.fit.readhub.services;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.iuh.fit.readhub.SavedBookRepository;
import com.iuh.fit.readhub.models.ReadingHistory;
import com.iuh.fit.readhub.models.SavedBook;
import com.iuh.fit.readhub.repositories.ReadingHistoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Service
public class RecommendationService {

    private static final int MAX_RECOMMENDATIONS = 20;
    private static final String GUTENDEX_API = "https://gutendex.com/books";

    // Cache cho book details để tránh gọi API nhiều lần
    private static final Map<Long, JsonNode> bookDetailsCache = new ConcurrentHashMap<>();
    private static final Map<String, List<Long>> subjectBooksCache = new ConcurrentHashMap<>();
    private static List<Long> popularBooksCache = null;
    private static long popularBooksCacheTime = 0;
    private static final long CACHE_DURATION = 1800000; // 30 phút

    @Autowired
    private SavedBookRepository savedBookRepository;

    @Autowired
    private ReadingHistoryRepository readingHistoryRepository;

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    @Cacheable(value = "recommendations", key = "#userId")
    public List<Long> getRecommendedBookIds(Long userId) {
        try {
            // Lấy danh sách sách của user
            List<Long> userBookIds = getUserBooks(userId);
            if (userBookIds.isEmpty()) {
                return getCachedPopularBooks();
            }

            // Lấy book details và subjects bất đồng bộ
            List<CompletableFuture<Map<String, Integer>>> subjectFutures = new ArrayList<>();
            List<CompletableFuture<Map<String, Integer>>> authorFutures = new ArrayList<>();

            for (Long bookId : userBookIds.subList(0, Math.min(5, userBookIds.size()))) {
                subjectFutures.add(analyzeBookSubjectsAsync(bookId));
                authorFutures.add(analyzeBookAuthorsAsync(bookId));
            }

            // Gộp kết quả subjects
            Map<String, Integer> subjectFrequencyMap = new HashMap<>();
            CompletableFuture.allOf(subjectFutures.toArray(new CompletableFuture[0])).join();
            for (CompletableFuture<Map<String, Integer>> future : subjectFutures) {
                future.get().forEach((k, v) -> subjectFrequencyMap.merge(k, v, Integer::sum));
            }

            // Gộp kết quả authors
            Map<String, Integer> authorFrequencyMap = new HashMap<>();
            CompletableFuture.allOf(authorFutures.toArray(new CompletableFuture[0])).join();
            for (CompletableFuture<Map<String, Integer>> future : authorFutures) {
                future.get().forEach((k, v) -> authorFrequencyMap.merge(k, v, Integer::sum));
            }

            // Lấy top subjects và authors
            List<String> topSubjects = subjectFrequencyMap.entrySet().stream()
                    .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                    .limit(3)
                    .map(Map.Entry::getKey)
                    .collect(Collectors.toList());

            List<String> topAuthors = authorFrequencyMap.entrySet().stream()
                    .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                    .limit(3)
                    .map(Map.Entry::getKey)
                    .collect(Collectors.toList());

            // Tìm sách theo subjects và authors bất đồng bộ
            Set<Long> recommendedBooks = ConcurrentHashMap.newKeySet();
            List<CompletableFuture<Void>> searchFutures = new ArrayList<>();

            // Thêm tìm kiếm theo subjects
            searchFutures.addAll(topSubjects.stream()
                    .map(subject -> findBooksBySubjectAsync(subject, recommendedBooks, new HashSet<>(userBookIds)))
                    .collect(Collectors.toList()));

            // Thêm tìm kiếm theo authors
            searchFutures.addAll(topAuthors.stream()
                    .map(author -> findBooksByAuthorAsync(author, recommendedBooks, new HashSet<>(userBookIds)))
                    .collect(Collectors.toList()));

            // Đợi tất cả hoàn thành
            CompletableFuture.allOf(searchFutures.toArray(new CompletableFuture[0])).join();

            // Nếu chưa đủ, thêm sách phổ biến
            if (recommendedBooks.size() < MAX_RECOMMENDATIONS) {
                addPopularBooks(recommendedBooks, new HashSet<>(userBookIds));
            }

            return new ArrayList<>(recommendedBooks).stream()
                    .limit(MAX_RECOMMENDATIONS)
                    .collect(Collectors.toList());

        } catch (Exception e) {
            e.printStackTrace();
            return getCachedPopularBooks();
        }
    }

    private List<Long> getUserBooks(Long userId) {
        Set<Long> userBooks = new LinkedHashSet<>();
        readingHistoryRepository.findByUser_UserId(userId)
                .forEach(history -> userBooks.add(history.getBookId()));
        savedBookRepository.findByUser_UserIdOrderBySavedAtDesc(userId)
                .forEach(saved -> userBooks.add(saved.getBookId()));
        return new ArrayList<>(userBooks);
    }

    @Async
    protected CompletableFuture<Map<String, Integer>> analyzeBookSubjectsAsync(Long bookId) {
        Map<String, Integer> subjectFrequency = new HashMap<>();
        try {
            JsonNode bookDetails = getBookDetails(bookId);
            if (bookDetails != null && bookDetails.has("subjects")) {
                bookDetails.get("subjects").forEach(subject -> {
                    String mainSubject = subject.asText().split("--")[0].trim();
                    if (!mainSubject.equals("Fiction")) {
                        subjectFrequency.merge(mainSubject, 1, Integer::sum);
                    }
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return CompletableFuture.completedFuture(subjectFrequency);
    }

    private JsonNode getBookDetails(Long bookId) {
        return bookDetailsCache.computeIfAbsent(bookId, id -> {
            try {
                String url = GUTENDEX_API + "/" + id;
                ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
                return objectMapper.readTree(response.getBody());
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        });
    }

    @Async
    protected CompletableFuture<Void> findBooksBySubjectAsync(String subject, Set<Long> recommendedBooks, Set<Long> userBooks) {
        try {
            List<Long> subjectBooks = subjectBooksCache.computeIfAbsent(subject, s -> {
                try {
                    String url = GUTENDEX_API + "?topic=" + s;
                    ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
                    JsonNode results = objectMapper.readTree(response.getBody()).get("results");
                    return StreamSupport.stream(results.spliterator(), false)
                            .map(book -> book.get("id").asLong())
                            .collect(Collectors.toList());
                } catch (Exception e) {
                    e.printStackTrace();
                    return new ArrayList<>();
                }
            });

            subjectBooks.stream()
                    .filter(id -> !userBooks.contains(id))
                    .forEach(recommendedBooks::add);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return CompletableFuture.completedFuture(null);
    }

    private synchronized List<Long> getCachedPopularBooks() {
        long currentTime = System.currentTimeMillis();
        if (popularBooksCache == null || currentTime - popularBooksCacheTime > CACHE_DURATION) {
            try {
                String url = GUTENDEX_API + "?sort=popular";
                ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
                JsonNode results = objectMapper.readTree(response.getBody()).get("results");
                popularBooksCache = StreamSupport.stream(results.spliterator(), false)
                        .map(book -> book.get("id").asLong())
                        .limit(MAX_RECOMMENDATIONS)
                        .collect(Collectors.toList());
                popularBooksCacheTime = currentTime;
            } catch (Exception e) {
                e.printStackTrace();
                return new ArrayList<>();
            }
        }
        return new ArrayList<>(popularBooksCache);
    }

    private void addPopularBooks(Set<Long> recommendedBooks, Set<Long> userBooks) {
        getCachedPopularBooks().stream()
                .filter(id -> !userBooks.contains(id))
                .forEach(recommendedBooks::add);
    }
    @Async
    protected CompletableFuture<Map<String, Integer>> analyzeBookAuthorsAsync(Long bookId) {
        Map<String, Integer> authorFrequency = new HashMap<>();
        try {
            JsonNode bookDetails = getBookDetails(bookId);
            if (bookDetails != null && bookDetails.has("authors")) {
                bookDetails.get("authors").forEach(author -> {
                    if (author.has("name")) {
                        String authorName = author.get("name").asText();
                        authorFrequency.merge(authorName, 1, Integer::sum);
                    }
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return CompletableFuture.completedFuture(authorFrequency);
    }

    @Async
    protected CompletableFuture<Void> findBooksByAuthorAsync(String author, Set<Long> recommendedBooks, Set<Long> userBooks) {
        try {
            String url = GUTENDEX_API + "?search=" + author.split(",")[0]; // Tìm theo họ của tác giả
            ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
            JsonNode results = objectMapper.readTree(response.getBody()).get("results");

            StreamSupport.stream(results.spliterator(), false)
                    .filter(book -> book.has("authors") &&
                            book.get("authors").findValues("name")
                                    .stream()
                                    .anyMatch(name -> name.asText().contains(author)))
                    .map(book -> book.get("id").asLong())
                    .filter(id -> !userBooks.contains(id))
                    .forEach(recommendedBooks::add);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return CompletableFuture.completedFuture(null);
    }
}