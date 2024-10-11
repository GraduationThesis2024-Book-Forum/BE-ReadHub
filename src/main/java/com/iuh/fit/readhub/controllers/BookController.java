package com.iuh.fit.readhub.controllers;

import com.iuh.fit.readhub.dto.ChapterDTO;
import com.iuh.fit.readhub.services.BookService;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("api/v1/book")
public class BookController {

    @Autowired
    private BookService bookService;

    @GetMapping("/chapters")
    public List<ChapterDTO> getChapters(@RequestParam String url) throws IOException {
        return bookService.getChaptersFromHtml(url);
    }

    @GetMapping("/chapter-content")
    public String getChapterContent(@RequestParam String url, @RequestParam String chapterId) throws IOException {
        return bookService.getChapterContent(url, chapterId);
    }

    @GetMapping("/book-content")
    public String getBookContent(@RequestParam String url, @RequestParam String urlImageCover) throws IOException {
        return bookService.getBookContent(url, urlImageCover);
    }

}
