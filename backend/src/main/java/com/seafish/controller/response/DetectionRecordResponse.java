package com.seafish.controller.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record DetectionRecordResponse(
        Long id,
        Long userId,
        String originalFileName,
        String originalImageUrl,
        String resultImageUrl,
        String modelName,
        BigDecimal confidenceThreshold,
        String status,
        String errorCode,
        String errorMessage,
        LocalDateTime startedAt,
        LocalDateTime completedAt,
        Long durationMs,
        LocalDateTime createdAt,
        List<DetectionResultResponse> results
) {
}
