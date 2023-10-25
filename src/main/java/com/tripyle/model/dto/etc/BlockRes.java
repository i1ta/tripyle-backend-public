package com.tripyle.model.dto.etc;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

public class BlockRes {

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class BlockDetailDto {
        Long id;
        String blockerUsername;
        String blockeeUsername;
        LocalDateTime regDateTime;
    }

}
