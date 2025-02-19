package com.iuh.fit.readhub.services;

import com.iuh.fit.readhub.dto.request.BookmarkRequest;
import com.iuh.fit.readhub.models.Bookmark;
import com.iuh.fit.readhub.repositories.BookmarkRepository;
import com.iuh.fit.readhub.repositories.UserRepository;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@AllArgsConstructor
public class BookmarkService {
    @Autowired
    private BookmarkRepository bookmarkRepository;

    @Autowired
    private UserRepository userRepository;

    public void createBookmark(BookmarkRequest bookmarkRequest) {
        Bookmark bookmark = new Bookmark();
        bookmark.setUser(userRepository.findById(bookmarkRequest.getUserId()).get());
        bookmark.setBookId(bookmarkRequest.getBookId());
        bookmark.setLocation(bookmarkRequest.getLocation());
        bookmarkRepository.save(bookmark);
    }

    public List<Bookmark> getBookmarksByUserIdAndBookId(Long userId, Long bookId) {
        return bookmarkRepository.findByUserIdAndBookId(userId, bookId);
    }

    public void removeBookmark(Long id) {
        bookmarkRepository.deleteById(id);
    }
}