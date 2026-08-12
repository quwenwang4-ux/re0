package com.seafish.controller.response;

import java.math.BigDecimal;

public record DetectionResultResponse(
        Long id,
        Long fishId,
        String className,
        BigDecimal confidence,
        BigDecimal boxX1,
        BigDecimal boxY1,
        BigDecimal boxX2,
        BigDecimal boxY2,
        String chineseName,
        String scientificName,
        String category,
        String habits,
        String habitat,
        String distribution,
        String protectionLevel,
        boolean fishProfileMissing
) {
}
