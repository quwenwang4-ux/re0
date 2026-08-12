package com.seafish.controller;

import com.seafish.common.ApiResponse;
import com.seafish.common.PageResponse;
import com.seafish.controller.request.ReviewFishProfileTaskRequest;
import com.seafish.entity.FishProfileTask;
import com.seafish.service.FishProfileTaskService;
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
@RequestMapping("/api/admin/fish-profile-tasks")
@PreAuthorize("hasRole('ADMIN')")
public class AdminFishProfileTaskController {

    private final FishProfileTaskService service;

    public AdminFishProfileTaskController(
            FishProfileTaskService service
    ) {
        this.service = service;
    }

    @GetMapping
    public ApiResponse<PageResponse<FishProfileTask>> list(
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "1") long page,
            @RequestParam(defaultValue = "10") long size
    ) {
        return ApiResponse.success(service.list(status, page, size));
    }

    @PutMapping("/{taskId}/review")
    public ApiResponse<FishProfileTask> review(
            @PathVariable Long taskId,
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody ReviewFishProfileTaskRequest request
    ) {
        return ApiResponse.success(service.review(
                taskId,
                Long.valueOf(jwt.getSubject()),
                request
        ));
    }
}
