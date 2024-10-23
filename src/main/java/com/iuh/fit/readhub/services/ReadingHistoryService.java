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

    public void createReadingHistory(ReadingHistoryRequest request) {
        ReadingHistory history = readingHistoryRepository
                .findByUserIdAndBookId(request.getUserId(), request.getBookId());
        if(history == null) {
            history = new ReadingHistory();
        }
        if (history.getHistoryId() == null) {
            history.setUser(userRepository.findById(request.getUserId()).get());
            history.setBookId(request.getBookId());
            history.setTimeSpent(request.getTimeSpent());
        } else {
            history.setTimeSpent(history.getTimeSpent() + request.getTimeSpent());
        }

        readingHistoryRepository.save(history);
    }


    public List<ReadingHistory> getReadingHistoryByUserId(Long userId) {
        return readingHistoryRepository.findByUser(userRepository.findById(userId).get());
    }
}
