package com.seafish.controller;

import com.seafish.common.ApiResponse;
import com.seafish.common.PageResponse;
import com.seafish.controller.request.ReviewFishInfoApplicationRequest;
import com.seafish.entity.FishInfoApplication;
import com.seafish.service.FishInfoApplicationService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/fish-applications")
@PreAuthorize("hasRole('ADMIN')")
public class AdminFishInfoApplicationController {

    private final FishInfoApplicationService service;

    public AdminFishInfoApplicationController(
            FishInfoApplicationService service
    ) {
        this.service = service;
    }

    @GetMapping
    public ApiResponse<PageResponse<FishInfoApplication>> list(
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "1") long page,
            @RequestParam(defaultValue = "10") long size
    ) {
        return ApiResponse.success(
                service.listApplications(status, page, size)
        );
    }

    @PutMapping("/{applicationId}/review")
    public ApiResponse<FishInfoApplication> review(
            @PathVariable Long applicationId,
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody
            ReviewFishInfoApplicationRequest request
    ) {
        return ApiResponse.success(service.review(
                applicationId,
                Long.valueOf(jwt.getSubject()),
                request
        ));
    }
}
