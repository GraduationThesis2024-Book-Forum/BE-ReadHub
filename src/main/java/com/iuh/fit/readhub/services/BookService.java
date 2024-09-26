package com.iuh.fit.readhub.services;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Service
public class BookService {

    private List<String> chapters;

    public BookService() throws IOException {
        chapters = new ArrayList<>();
        loadChaptersFromHtml();
    }

    private void loadChaptersFromHtml() throws IOException {
        String url = "https://www.gutenberg.org/cache/epub/145/pg145-images.html";
        Document doc = Jsoup.connect(url).get();

        // Tìm tất cả các thẻ div với class "chapter"
        Elements chapterElements = doc.select("div.chapter");

        // Lưu lại nội dung của mỗi chương
        for (Element chapterElement : chapterElements) {
            chapters.add(chapterElement.html());  // Lưu nội dung HTML của chương
        }
    }

    public String getChapter(int chapterNumber) {
        if (chapterNumber < 1 || chapterNumber > chapters.size()) {
            return "Chương không tồn tại!";
        }
        return chapters.get(chapterNumber - 1);  // Trả về chương tương ứng
    }

    public int getTotalChapters() {
        return chapters.size();  // Trả về tổng số chương
    }
}
