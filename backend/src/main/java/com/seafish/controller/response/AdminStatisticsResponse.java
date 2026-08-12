package com.seafish.controller.response;

import java.util.List;

public record AdminStatisticsResponse(
        long activeUserCount,
        long detectionCount,
        long rescueOrderCount,
        long completedRescueOrderCount,
        List<LabelCountResponse> usersByRole,
        List<LabelCountResponse> detectionsByStatus,
        List<LabelCountResponse> topDetectedClasses,
        List<LabelCountResponse> rescueOrdersByStatus,
        List<LabelCountResponse> volunteerAcceptedCounts,
        List<LabelCountResponse> volunteerCompletedCounts
) {
}
