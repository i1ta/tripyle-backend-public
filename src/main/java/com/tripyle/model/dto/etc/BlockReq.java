package com.tripyle.model.dto.etc;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

public class BlockReq {

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class BlockeeIdDto {
        Long blockeeId;
    }
}