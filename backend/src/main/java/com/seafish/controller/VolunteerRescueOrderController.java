package com.seafish.controller;

import com.seafish.common.ApiResponse;
import com.seafish.controller.response.RescueOrderResponse;
import com.seafish.service.RescueOrderService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.seafish.common.PageResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import com.seafish.controller.request.CompleteRescueOrderRequest;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.RequestBody;

@RestController
@RequestMapping("/api/volunteer/rescue-orders")
@PreAuthorize("hasRole('VOLUNTEER')")
public class VolunteerRescueOrderController {

    private final RescueOrderService
            rescueOrderService;

    public VolunteerRescueOrderController(
            RescueOrderService rescueOrderService
    ) {
        this.rescueOrderService =
                rescueOrderService;
    }

    @GetMapping("/me")
    public ApiResponse<PageResponse<RescueOrderResponse>>
    getMyOrders(
            @AuthenticationPrincipal Jwt jwt,

            @RequestParam(defaultValue = "1")
            long page,

            @RequestParam(defaultValue = "10")
            long size
    ) {
        Long volunteerId =
                Long.valueOf(jwt.getSubject());

        PageResponse<RescueOrderResponse> response =
                rescueOrderService.getVolunteerOrders(
                        volunteerId,
                        page,
                        size
                );

        return ApiResponse.success(response);
    }

    @PutMapping("/{orderId}/accept")
    public ApiResponse<RescueOrderResponse>
    acceptOrder(
            @PathVariable Long orderId,
            @AuthenticationPrincipal Jwt jwt
    ) {
        Long volunteerId =
                Long.valueOf(jwt.getSubject());

        RescueOrderResponse response =
                rescueOrderService.acceptOrder(
                        orderId,
                        volunteerId
                );

        return ApiResponse.success(response);
    }

    @PutMapping("/{orderId}/completion")
    public ApiResponse<RescueOrderResponse>
    submitCompletion(
            @PathVariable Long orderId,
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody
            CompleteRescueOrderRequest request
    ) {
        Long volunteerId =
                Long.valueOf(jwt.getSubject());

        RescueOrderResponse response =
                rescueOrderService.submitCompletion(
                        orderId,
                        volunteerId,
                        request
                );

        return ApiResponse.success(response);
    }
}