package com.iuh.fit.readhub.services;

import com.iuh.fit.readhub.dto.request.ReadingHistoryRequest;
import com.iuh.fit.readhub.models.ReadingHistory;
import com.iuh.fit.readhub.repositories.ReadingHistoryRepository;
import com.iuh.fit.readhub.repositories.UserRepository;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@AllArgsConstructor
public class ReadingHistoryService {
    @Autowired
    private ReadingHistoryRepository readingHistoryRepository;

    @Autowired
    private UserRepository userRepository;

    public void createReadingHistory(ReadingHistoryRequest readingHistoryRequest) {
        ReadingHistory readingHistory = new ReadingHistory();
        readingHistory.setUser(userRepository.findById(readingHistoryRequest.getUserId()).get());
        readingHistory.setBookId(readingHistoryRequest.getBookId());
        readingHistoryRepository.save(readingHistory);
    }

    public List<ReadingHistory> getReadingHistoryByUserId(Long userId) {
        return readingHistoryRepository.findByUser(userRepository.findById(userId).get());
    }
}
