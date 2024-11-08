package com.iuh.fit.readhub.services;

import com.iuh.fit.readhub.SavedBookRepository;
import com.iuh.fit.readhub.models.SavedBook;
import com.iuh.fit.readhub.models.User;
import com.iuh.fit.readhub.repositories.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class SavedBookService {

    @Autowired
    private SavedBookRepository savedBookRepository;

    @Autowired
    private UserRepository userRepository;

    public void saveBook(Long userId, Long bookId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (savedBookRepository.existsByUser_UserIdAndBookId(userId, bookId)) {
            throw new RuntimeException("Book already saved");
        }

        SavedBook savedBook = new SavedBook();
        savedBook.setUser(user);
        savedBook.setBookId(bookId);
        savedBookRepository.save(savedBook);
    }


    public void unsaveBook(Long userId, Long bookId) {
        Optional<SavedBook> savedBook= savedBookRepository.findByUser_UserIdAndBookId(userId, bookId);
        if (savedBook.isPresent()) {
            savedBookRepository.delete(savedBook.get());
        }
    }

    public List<Long> getSavedBookIds(Long userId) {
        return savedBookRepository.findByUser_UserIdOrderBySavedAtDesc(userId)
                .stream()
                .map(SavedBook::getBookId)
                .collect(Collectors.toList());
    }

    public boolean isBookSaved(Long userId, Long bookId) {
        return savedBookRepository.existsByUser_UserIdAndBookId(userId, bookId);
    }
}