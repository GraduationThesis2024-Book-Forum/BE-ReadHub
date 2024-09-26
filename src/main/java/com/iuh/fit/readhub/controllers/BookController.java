package com.iuh.fit.readhub.controllers;

import com.iuh.fit.readhub.services.BookService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api/v1/book")
public class BookController {

    @Autowired
    private BookService bookService;

    @GetMapping("/chapter")
    public String getChapter(@RequestParam int chapterNumber) {
        // Trả về nội dung của chương dựa trên số chương
        return bookService.getChapter(chapterNumber);
    }

    @GetMapping("/total-chapters")
    public int getTotalChapters() {
        // Trả về tổng số chương của cuốn sách
        return bookService.getTotalChapters();
    }
}
