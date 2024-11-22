package com.iuh.fit.readhub.mapper;

import com.iuh.fit.readhub.dto.ForumDTO;
import com.iuh.fit.readhub.dto.ForumReportDTO;
import com.iuh.fit.readhub.models.ForumReport;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ForumReportMapper {
    private final UserMapper userMapper;

    public ForumReportDTO toDTO(ForumReport report) {
        return ForumReportDTO.builder()
                .id(report.getId())
                .forum(ForumDTO.builder()
                        .discussionId(report.getForum().getDiscussionId())
                        .forumTitle(report.getForum().getForumTitle())
                        .forumDescription(report.getForum().getForumDescription())
                        .imageUrl(report.getForum().getImageUrl())
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