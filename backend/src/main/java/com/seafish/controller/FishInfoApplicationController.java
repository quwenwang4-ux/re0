package com.seafish.controller;

import com.seafish.common.ApiResponse;
import com.seafish.common.PageResponse;
import com.seafish.controller.request.CreateFishInfoApplicationRequest;
import com.seafish.entity.FishInfoApplication;
import com.seafish.service.FishInfoApplicationService;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/fish-applications")
public class FishInfoApplicationController {

    private final FishInfoApplicationService service;

    public FishInfoApplicationController(
            FishInfoApplicationService service
    ) {
        this.service = service;
    }

    @PostMapping
    public ApiResponse<FishInfoApplication> create(
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody
            CreateFishInfoApplicationRequest request
    ) {
        return ApiResponse.success(service.create(
                Long.valueOf(jwt.getSubject()),
                request
        ));
    }

    @GetMapping("/me")
    public ApiResponse<PageResponse<FishInfoApplication>>
    getMyApplications(
            @AuthenticationPrincipal Jwt jwt,
            @RequestParam(defaultValue = "1") long page,
            @RequestParam(defaultValue = "10") long size
    ) {
        return ApiResponse.success(service.getMyApplications(
                Long.valueOf(jwt.getSubject()),
                page,
                size
        ));
    }
}
