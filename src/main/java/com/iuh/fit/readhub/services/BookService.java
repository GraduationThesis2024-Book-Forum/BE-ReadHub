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
    public List<String> getChaptersFromHtml(String url) throws IOException {
        List<String> chapters = new ArrayList<>();
        Document doc = Jsoup.connect(url).get();

        Elements chapterElements = doc.select("a.pginternal");

        for (Element chapterElement : chapterElements) {
            String chapterName = chapterElement.text();  // Lấy tên chương
            chapters.add(chapterName);
        }

        return chapters;
    }
}
