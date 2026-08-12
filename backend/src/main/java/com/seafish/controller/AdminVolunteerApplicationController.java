package com.seafish.controller;

import com.seafish.common.ApiResponse;
import com.seafish.common.PageResponse;
import com.seafish.controller.response.VolunteerApplicationResponse;
import com.seafish.service.VolunteerApplicationService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.seafish.controller.request.ReviewVolunteerApplicationRequest;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;

@RestController
@RequestMapping("/api/admin/volunteer-applications")
@PreAuthorize("hasRole('ADMIN')")
public class AdminVolunteerApplicationController {

    private final VolunteerApplicationService
            volunteerApplicationService;

    public AdminVolunteerApplicationController(
            VolunteerApplicationService
                    volunteerApplicationService
    ) {
        this.volunteerApplicationService =
                volunteerApplicationService;
    }

    @GetMapping
    public ApiResponse<
            PageResponse<VolunteerApplicationResponse>
            > listApplications(
            @RequestParam(required = false)
            String status,

            @RequestParam(defaultValue = "1")
            long page,

            @RequestParam(defaultValue = "10")
            long size
    ) {
        PageResponse<VolunteerApplicationResponse>
                response =
                volunteerApplicationService
                        .listApplications(
                                status,
                                page,
                                size
                        );

        return ApiResponse.success(response);
    }

    @PutMapping("/{applicationId}/review")
    public ApiResponse<VolunteerApplicationResponse>
    reviewApplication(
            @PathVariable Long applicationId,
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody
            ReviewVolunteerApplicationRequest request
    ) {
        Long reviewerId =
                Long.valueOf(jwt.getSubject());

        VolunteerApplicationResponse response =
                volunteerApplicationService
                        .reviewApplication(
                                applicationId,
                                reviewerId,
                                request
                        );

        return ApiResponse.success(response);
    }
}