package com.seafish.controller;

import com.seafish.common.ApiResponse;
import com.seafish.controller.response.AdminStatisticsResponse;
import com.seafish.service.StatisticsService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;

@RestController
@RequestMapping("/api/admin/statistics")
@PreAuthorize("hasRole('ADMIN')")
public class AdminStatisticsController {

    private final StatisticsService statisticsService;

    public AdminStatisticsController(
            StatisticsService statisticsService
    ) {
        this.statisticsService = statisticsService;
    }

    @GetMapping("/overview")
    public ApiResponse<AdminStatisticsResponse> overview(
            @AuthenticationPrincipal Jwt jwt
    ) {
        return ApiResponse.success(
                statisticsService.getOverview(
                        Long.valueOf(jwt.getSubject())
                )
        );
    }
}
