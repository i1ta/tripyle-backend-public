package com.tripyle.model.dto.etc;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

public class ReportRes {

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ReasonListDto {
        Long id;
        String reason;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ReportDetailDto {
        Long id;
        String reporterUsername;
        String reporteeUsername;
        String reportReason;
        String content;
        LocalDateTime regDateTime;
    }

}
