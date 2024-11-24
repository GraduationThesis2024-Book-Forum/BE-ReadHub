package com.iuh.fit.readhub.mapper;

import com.iuh.fit.readhub.dto.DiscussionDTO;
import com.iuh.fit.readhub.dto.DiscussionReportDTO;
import com.iuh.fit.readhub.models.DiscussionReport;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DiscussionReportMapper {
    private final UserMapper userMapper;

    public DiscussionReportDTO toDTO(DiscussionReport report) {
        return DiscussionReportDTO.builder()
                .id(report.getId())
                .forum(DiscussionDTO.builder()
                        .discussionId(report.getDiscussion().getDiscussionId())
                        .forumTitle(report.getDiscussion().getForumTitle())
                        .forumDescription(report.getDiscussion().getForumDescription())
                        .imageUrl(report.getDiscussion().getImageUrl())
                        .build())
                .reporter(userMapper.toDTO(report.getReporter()))
                .reason(report.getReason())
                .additionalInfo(report.getAdditionalInfo())
                .status(report.getStatus())
                .reportedAt(report.getReportedAt())
                .resolvedAt(report.getResolvedAt())
                .build();
    }
}