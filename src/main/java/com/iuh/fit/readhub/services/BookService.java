package com.iuh.fit.readhub.services;

import com.iuh.fit.readhub.dto.ChapterDTO;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class BookService {

    private final ConcurrentHashMap<String, Document> htmlCache = new ConcurrentHashMap<>();

    private Document getHtmlFromUrl(String url) throws IOException {
        if (htmlCache.containsKey(url)) {
            return htmlCache.get(url);
        }
        Document doc = Jsoup.connect(url)
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
}
