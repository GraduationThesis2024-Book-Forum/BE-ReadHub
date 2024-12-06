package com.iuh.fit.readhub.services;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.iuh.fit.readhub.dto.AuthorDTO;
import com.iuh.fit.readhub.dto.BookDTO;
import com.iuh.fit.readhub.dto.BookSearchResponse;
import com.iuh.fit.readhub.dto.ChapterDTO;
import com.iuh.fit.readhub.dto.request.BookSearchRequest;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

@Service
@Slf4j
@AllArgsConstructor
public class BookService {

    private final String baseUrl="https://gutendex.com";
    private final ObjectMapper objectMapper;

    private final ConcurrentHashMap<String, Document> htmlCache = new ConcurrentHashMap<>();

    private Document getHtmlFromUrl(String url) throws IOException {
        if (htmlCache.containsKey(url)) {
            return htmlCache.get(url);
        }
        Document doc = Jsoup.connect(url).timeout(15000)
                .get();
        htmlCache.put(url, doc);
        return doc;
    }

    public List<ChapterDTO> getChaptersFromHtml(String url) throws IOException {
        List<ChapterDTO> chapters = new ArrayList<>();
        Document doc = getHtmlFromUrl(url);

        Elements chapterElements = doc.select("a.pginternal");
        for (Element chapterElement : chapterElements) {
            String chapterName = chapterElement.text();
            String chapterHref = chapterElement.attr("href");

            String chapterId = chapterHref.startsWith("#") ? chapterHref.substring(1) : chapterHref;
            chapters.add(new ChapterDTO(chapterId, chapterName));
        }
        return chapters;
    }

    public String getChapterContent(String url, String chapterId) throws IOException {
        Document doc = getHtmlFromUrl(url);
        Elements chapterAnchors = doc.select("a[id=" + chapterId + "]");
        if (chapterAnchors.isEmpty()) {
            return "Chương không tồn tại!";
        }

        Element chapterAnchor = chapterAnchors.first();
        Element chapterDiv = chapterAnchor.parent();
        StringBuilder chapterContent = new StringBuilder();

        while (chapterDiv != null) {
            chapterContent.append(chapterDiv.outerHtml());
            if (chapterDiv.toString().contains("<!--end chapter-->")) {
                break;
            }
            chapterDiv = chapterDiv.nextElementSibling();
        }
        return chapterContent.toString();
    }

    public String getBookContent(String url,String urlImageCover) throws IOException {
        Document doc = getHtmlFromUrl(url);
        String contents = doc.select("body").toString().replace("images/cover.jpg", urlImageCover);
        return contents;
    }
    public BookSearchResponse searchBooks(BookSearchRequest request) throws IOException {
        // First, we build our search URL with the basic criteria
        StringBuilder searchUrl = new StringBuilder(baseUrl + "/books/?");
        List<String> params = new ArrayList<>();

        // Combine title and author for the general search
        if (StringUtils.hasText(request.getTitle()) || StringUtils.hasText(request.getAuthor())) {
            String searchTerm = Stream.of(request.getTitle(), request.getAuthor())
                    .filter(StringUtils::hasText)
                    .collect(Collectors.joining(" "));
            params.add("search=" + URLEncoder.encode(searchTerm, StandardCharsets.UTF_8));
        }

        // Add language filter if specified
        if (request.getLanguages() != null && !request.getLanguages().isEmpty()) {
            params.add("languages=" + request.getLanguages().stream()
                    .collect(Collectors.joining(",")));
        }

        // Add pagination
        int page = request.getPage() != null ? request.getPage() : 1;
        params.add("page=" + page);



        // Construct final URL
        searchUrl.append(String.join("&", params));

        // Execute the HTTP request
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest httpRequest = HttpRequest.newBuilder()
                .uri(URI.create(searchUrl.toString()))
                .GET()
                .build();

        try {
            HttpResponse<String> response = client.send(httpRequest, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                log.error("Failed to search books. Status: {}. Body: {}",
                        response.statusCode(), response.body());
                return BookSearchResponse.builder()
                        .books(Collections.emptyList())
                        .totalPages(0)
                        .totalElements(0)
                        .hasNext(false)
                        .build();
            }

            // Add debug logging
            log.debug("Received response from Gutendex: {}", response.body());

            JsonNode rootNode = objectMapper.readTree(response.body());
            return processSearchResults(rootNode, request);

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("Search interrupted", e);
            throw new IOException("Search interrupted", e);
        } catch (IOException e) {
            log.error("Error searching books", e);
            throw e;
        }
    }

    /**
     * Processes and filters the raw results from Gutendex
     */
    private BookSearchResponse processSearchResults(JsonNode response, BookSearchRequest request) {
        // First get all books from the response
        List<BookDTO> books = StreamSupport.stream(response.get("results").spliterator(), false)
                .map(this::mapToBook)
                .filter(Objects::nonNull)  // Remove any null books from mapping failures
                .filter(book -> {
                    // If we have a title search, we should ONLY match titles
                    if (StringUtils.hasText(request.getTitle())) {
                        // Case-insensitive title matching
                        return book.getTitle().toLowerCase()
                                .contains(request.getTitle().toLowerCase());
                    }

                    // If we have an author search, we should ONLY match author names
                    if (StringUtils.hasText(request.getAuthor())) {
                        return book.getAuthors().stream()
                                .map(author -> author.getName().toLowerCase())
                                .anyMatch(name -> name.contains(
                                        request.getAuthor().toLowerCase()));
                    }

                    // If no specific search criteria, include all books
                    return true;
                })
                .collect(Collectors.toList());

        // Handle pagination of filtered results
        int pageSize = request.getSize() != null ? request.getSize() : 32;
        int totalElements = books.size();
        int totalPages = (int) Math.ceil((double) totalElements / pageSize);

        // Calculate the current page's books
        int start = ((request.getPage() != null ? request.getPage() : 1) - 1) * pageSize;
        List<BookDTO> paginatedBooks = books.stream()
                .skip(start)
                .limit(pageSize)
                .collect(Collectors.toList());

        boolean hasNext= true;

        if (totalPages <2) {
            hasNext = false;
        }

        return BookSearchResponse.builder()
                .books(paginatedBooks)
                .totalPages(totalPages)
                .totalElements(totalElements)
                .hasNext(hasNext)
                .build();
    }

    /**
     * Applies all filters to a book to determine if it matches search criteria
     */
    private boolean filterBook(BookDTO book, BookSearchRequest request) {
        return matchesTitle(book.getTitle(), request.getTitle()) &&
                matchesAuthor(book.getAuthors(), request.getAuthor()) &&
                matchesGenre(book.getGenres(), request.getGenres());
    }

    /**
     * Checks if a book title matches the search criteria
     */
    private boolean matchesTitle(String bookTitle, String searchTitle) {
        if (!StringUtils.hasText(searchTitle)) return true;
        return normalizeString(bookTitle)
                .contains(normalizeString(searchTitle));
    }

    /**
     * Checks if any of the book's authors match the search criteria
     */
    private boolean matchesAuthor(List<AuthorDTO> authors, String searchAuthor) {
        if (!StringUtils.hasText(searchAuthor)) return true;
        String normalizedSearch = normalizeString(searchAuthor);
        return authors.stream()
                .map(author -> normalizeString(author.getName()))
                .anyMatch(name -> name.contains(normalizedSearch));
    }

    /**
     * Checks if any of the book's genres match the search criteria
     */
    private boolean matchesGenre(List<String> bookGenres, List<String> searchGenre) {
        if (searchGenre.size()<0) return true;
        String normalizedSearch = searchGenre.stream()
                .map(this::normalizeString)
                .collect(Collectors.joining(" "));
        return bookGenres.stream()
                .map(this::normalizeString)
                .anyMatch(genre -> genre.contains(normalizedSearch));
    }

    /**
     * Normalizes strings for consistent comparison
     */
    private String normalizeString(String input) {
        return input.toLowerCase()
                .trim()
                .replaceAll("\\s+", " ");
    }

    /**
     * Maps the JSON response to our Book model
     */
    private BookDTO mapToBook(JsonNode bookNode) {
        try {
            // Basic validation of required fields
            if (bookNode == null || !bookNode.isObject()) {
                return null;
            }

            // Extract and validate required fields
            if (!bookNode.has("id") || !bookNode.has("title")) {
                log.warn("Book missing required fields: {}", bookNode);
                return null;
            }

            // Handle the formats node to get cover image URL
            String coverUrl = null;
            JsonNode formats = bookNode.get("formats");
            if (formats != null && formats.has("image/jpeg")) {
                coverUrl = formats.get("image/jpeg").asText();
            }

            // Get the first language or default to "en"
            String language = "en";
            JsonNode languages = bookNode.get("languages");
            if (languages != null && languages.isArray() && languages.size() > 0) {
                language = languages.get(0).asText();
            }

            // Build the book object with all extracted information
            return BookDTO.builder()
                    .id(bookNode.get("id").asInt())
                    .title(bookNode.get("title").asText())
                    .authors(bookNode.has("authors") ? mapAuthors(bookNode.get("authors")) : Collections.emptyList())
                    // Handle bookshelves/genres carefully
                    .genres(bookNode.has("bookshelves") ? mapGenres(bookNode.get("bookshelves")) : Collections.emptyList())
                    .language(language)
                    .coverUrl(coverUrl)
                    .build();

        } catch (Exception e) {
            // Log the full error context for debugging
            log.error("Error mapping book from JSON: {}", bookNode, e);
            return null;
        }
    }


//    mapAuthors

    private List<AuthorDTO> mapAuthors(JsonNode authorsNode) {
        return StreamSupport.stream(authorsNode.spliterator(), false)
                .map(authorNode -> AuthorDTO.builder()
                        .name(authorNode.get("name").asText())
                        .birthYear(authorNode.has("birth_year") ? authorNode.get("birth_year").asInt() : null)
                        .deathYear(authorNode.has("death_year") ? authorNode.get("death_year").asInt() : null)
                        .build())
                .collect(Collectors.toList());
    }
//    mapGenres

    private List<String> mapGenres(JsonNode genresNode) {
        // If the genres node is null, return an empty list
        if (genresNode == null) {
            return Collections.emptyList();
        }

        try {
            // Convert JsonNode array to a stream
            return StreamSupport.stream(genresNode.spliterator(), false)
                    // Each node in bookshelves is already a text value
                    .map(JsonNode::asText)  // Convert directly to text since they're string values
                    // Remove empty or null values
                    .filter(StringUtils::hasText)
                    // Clean up the genre names by removing the "Browsing: " prefix
                    .map(genre -> genre.replaceFirst("^Browsing: ", ""))
                    .collect(Collectors.toList());
        } catch (Exception e) {
            // Log the error with the problematic node for debugging
            log.warn("Error mapping genres. Node content: {}", genresNode, e);
            return Collections.emptyList();
        }
    }


}

