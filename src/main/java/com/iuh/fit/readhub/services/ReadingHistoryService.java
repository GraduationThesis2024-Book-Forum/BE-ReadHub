package com.iuh.fit.readhub.services;

import com.iuh.fit.readhub.dto.request.ReadingHistoryRequest;
import com.iuh.fit.readhub.models.ReadingHistory;
import com.iuh.fit.readhub.repositories.ReadingHistoryRepository;
import com.iuh.fit.readhub.repositories.UserRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@AllArgsConstructor
public class ReadingHistoryService {
    @Autowired
    private ReadingHistoryRepository readingHistoryRepository;

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private ChallengeProgressService challengeProgressService;


    @Transactional
    public void createReadingHistory(ReadingHistoryRequest request) {
        log.info("Creating reading history for user: {} and book: {}",
                request.getUserId(), request.getBookId());

        ReadingHistory history = readingHistoryRepository
                .findByUserIdAndBookId(request.getUserId(), request.getBookId());

        if(history == null) {
            history = new ReadingHistory();
            log.info("Creating new reading history entry");
        } else {
            log.info("Updating existing reading history entry");
        }

        if (history.getHistoryId() == null) {
            history.setUser(userRepository.findById(request.getUserId()).get());
            history.setBookId(request.getBookId());
            history.setTimeSpent(request.getTimeSpent());
        } else {
            history.setTimeSpent(history.getTimeSpent() + request.getTimeSpent());
        }

        ReadingHistory savedHistory = readingHistoryRepository.save(history);
        log.info("Saved reading history with id: {}", savedHistory.getHistoryId());

        // Kiểm tra challenge progress
        challengeProgressService.checkChallengeProgress(request.getUserId());
    }

    public List<Long> getReadingHistoryIdsByUserId(Long userId) {
        return readingHistoryRepository.findByUser_UserIdOrderByUpdatedAtDesc(userId)
                .stream()
                .map(ReadingHistory::getBookId)
                .toList();
    }
}
