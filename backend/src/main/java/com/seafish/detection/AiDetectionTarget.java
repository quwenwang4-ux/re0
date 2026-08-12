package com.seafish.detection;

import java.math.BigDecimal;

public record AiDetectionTarget(
        String className,
        BigDecimal confidence,
        BigDecimal boxX1,
        BigDecimal boxY1,
        BigDecimal boxX2,
        BigDecimal boxY2
) {
}
