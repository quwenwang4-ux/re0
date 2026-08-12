package com.seafish.controller;

import com.seafish.common.ApiResponse;
import com.seafish.common.PageResponse;
import com.seafish.controller.request.ReviewRescueOrderRequest;
import com.seafish.controller.request.ConfirmRescueCompletionRequest;
import com.seafish.controller.response.RescueOrderResponse;
import com.seafish.service.RescueOrderService;
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
@RequestMapping("/api/admin/rescue-orders")
@PreAuthorize("hasRole('ADMIN')")
public class AdminRescueOrderController {

    private final RescueOrderService
            rescueOrderService;

    public AdminRescueOrderController(
            RescueOrderService rescueOrderService
    ) {
        this.rescueOrderService =
                rescueOrderService;
    }

    @GetMapping
    public ApiResponse<PageResponse<RescueOrderResponse>>
    listOrders(
            @RequestParam(required = false)
            String status,

            @RequestParam(defaultValue = "1")
            long page,

            @RequestParam(defaultValue = "10")
            long size
    ) {
        PageResponse<RescueOrderResponse> response =
                rescueOrderService.listOrders(
                        status,
                        page,
                        size
                );

        return ApiResponse.success(response);
    }

    @PutMapping("/{orderId}/review")
    public ApiResponse<RescueOrderResponse>
    reviewOrder(
            @PathVariable Long orderId,
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody
            ReviewRescueOrderRequest request
    ) {
        Long reviewerId =
                Long.valueOf(jwt.getSubject());

        RescueOrderResponse response =
                rescueOrderService.reviewOrder(
                        orderId,
                        reviewerId,
                        request
                );

        return ApiResponse.success(response);
    }

    @PutMapping("/{orderId}/completion-confirmation")
    public ApiResponse<RescueOrderResponse>
    confirmCompletion(
            @PathVariable Long orderId,
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody
            ConfirmRescueCompletionRequest request
    ) {
        Long confirmerId =
                Long.valueOf(jwt.getSubject());

        RescueOrderResponse response =
                rescueOrderService.confirmCompletion(
                        orderId,
                        confirmerId,
                        request
                );

        return ApiResponse.success(response);
    }
}
