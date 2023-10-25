package com.tripyle.model.dto.etc;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

public class ReportReq {

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ReportCreateDto {
        private Long reporteeId;
        private Long reportReasonId;
        private String content;
    }
}
