package com.iuh.fit.readhub.services;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.iuh.fit.readhub.SavedBookRepository;
import com.iuh.fit.readhub.repositories.ReadingHistoryRepository;
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
public class RecommendationService {
    private static final int MAX_RECOMMENDATIONS = 20;
    private static final String GUTENDEX_API = "https://gutendex.com/books";
    private static final long CACHE_DURATION = 3600000; // 1 giờ

    // Cache system
    private static final Map<Long, JsonNode> bookDetailsCache = new ConcurrentHashMap<>();
    private static final Map<String, List<Long>> subjectBooksCache = new ConcurrentHashMap<>();
    private static final Map<String, List<Long>> authorBooksCache = new ConcurrentHashMap<>();
    private static List<Long> popularBooksCache = null;
    private static long popularBooksCacheTime = 0;

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
            List<Long> userBookIds = getUserBooks(userId);
            if (userBookIds.isEmpty()) {
                return getCachedPopularBooks();
            }

            // Giảm số lượng sách phân tích xuống 3 cuốn gần nhất
            List<Long> recentBooks = userBookIds.subList(0, Math.min(3, userBookIds.size()));

            // Phân tích song song subjects và authors
            Map<String, Integer> subjectFrequencyMap = new ConcurrentHashMap<>();
            Map<String, Integer> authorFrequencyMap = new ConcurrentHashMap<>();
            List<CompletableFuture<?>> analysisFutures = new ArrayList<>();

            for (Long bookId : recentBooks) {
                CompletableFuture<JsonNode> bookDetailsFuture = CompletableFuture.supplyAsync(() -> getBookDetails(bookId));

                analysisFutures.add(bookDetailsFuture.thenAcceptAsync(bookDetails -> {
                    if (bookDetails != null) {
                        analyzeBookDetails(bookDetails, subjectFrequencyMap, authorFrequencyMap);
                    }
                }));
            }

            // Đợi phân tích hoàn thành
            CompletableFuture.allOf(analysisFutures.toArray(new CompletableFuture[0])).join();

            // Lấy top subjects và authors
            List<String> topSubjects = getTopEntries(subjectFrequencyMap, 2);
            List<String> topAuthors = getTopEntries(authorFrequencyMap, 2);

            // Tìm sách theo subjects và authors
            Set<Long> recommendedBooks = ConcurrentHashMap.newKeySet();
            Set<Long> userBooksSet = new HashSet<>(userBookIds);

            // Thực hiện song song việc tìm sách
            List<CompletableFuture<Void>> searchFutures = new ArrayList<>();

            // Tìm theo subjects
            for (String subject : topSubjects) {
                searchFutures.add(CompletableFuture.runAsync(() ->
                        findBooksBySubject(subject, recommendedBooks, userBooksSet)));
            }

            // Tìm theo authors
            for (String author : topAuthors) {
                searchFutures.add(CompletableFuture.runAsync(() ->
                        findBooksByAuthor(author, recommendedBooks, userBooksSet)));
            }

            // Thêm popular books song song nếu cần
            if (recommendedBooks.size() < MAX_RECOMMENDATIONS) {
                searchFutures.add(CompletableFuture.runAsync(() ->
                        addPopularBooks(recommendedBooks, userBooksSet)));
            }

            // Đợi tất cả hoàn thành
            CompletableFuture.allOf(searchFutures.toArray(new CompletableFuture[0])).join();

            return new ArrayList<>(recommendedBooks).stream()
                    .limit(MAX_RECOMMENDATIONS)
                    .collect(Collectors.toList());

        } catch (Exception e) {
            e.printStackTrace();
            return getCachedPopularBooks();
        }
    }

    private void analyzeBookDetails(JsonNode bookDetails,
                                    Map<String, Integer> subjectFrequencyMap,
                                    Map<String, Integer> authorFrequencyMap) {
        // Phân tích subjects
        if (bookDetails.has("subjects")) {
            bookDetails.get("subjects").forEach(subject -> {
                String mainSubject = subject.asText().split("--")[0].trim();
                if (!mainSubject.equals("Fiction")) {
                    subjectFrequencyMap.merge(mainSubject, 1, Integer::sum);
                }
            });
        }

        // Phân tích authors
        if (bookDetails.has("authors")) {
            bookDetails.get("authors").forEach(author -> {
                if (author.has("name")) {
                    String authorName = author.get("name").asText();
                    authorFrequencyMap.merge(authorName, 1, Integer::sum);
                }
            });
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

    private void findBooksBySubject(String subject, Set<Long> recommendedBooks, Set<Long> userBooks) {
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
    }

    private void findBooksByAuthor(String author, Set<Long> recommendedBooks, Set<Long> userBooks) {
        try {
            List<Long> authorBooks = authorBooksCache.computeIfAbsent(author, a -> {
                try {
                    String url = GUTENDEX_API + "?search=" + a.split(",")[0];
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

            authorBooks.stream()
                    .filter(id -> !userBooks.contains(id))
                    .forEach(recommendedBooks::add);
        } catch (Exception e) {
            e.printStackTrace();
        }
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
}