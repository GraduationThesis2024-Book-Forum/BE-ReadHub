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
        readingHistory.setTimeSpent(readingHistoryRequest.getTimeSpent());
        readingHistoryRepository.save(readingHistory);
    }

//    update
    public void updateReadingHistory(ReadingHistoryRequest readingHistoryRequest) {
        ReadingHistory readingHistory = readingHistoryRepository.findById(readingHistoryRequest.getReadingHistoryId()).get();
        readingHistory.setUser(userRepository.findById(readingHistoryRequest.getUserId()).get());
        readingHistory.setBookId(readingHistoryRequest.getBookId());
        if (readingHistory.getTimeSpent() == null) {
            readingHistory.setTimeSpent(readingHistoryRequest.getTimeSpent());
        } else {
            readingHistory.setTimeSpent(readingHistory.getTimeSpent() + readingHistoryRequest.getTimeSpent());
        }
        readingHistoryRepository.save(readingHistory);
    }

    public List<ReadingHistory> getReadingHistoryByUserId(Long userId) {
        return readingHistoryRepository.findByUser(userRepository.findById(userId).get());
    }
}
