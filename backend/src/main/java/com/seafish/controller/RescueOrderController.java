package com.seafish.controller;

import com.seafish.common.ApiResponse;
import com.seafish.controller.request.CreateRescueOrderRequest;
import com.seafish.controller.response.RescueOrderResponse;
import com.seafish.service.RescueOrderService;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.seafish.common.PageResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import com.seafish.controller.response.PublicRescueOrderResponse;

@RestController
@RequestMapping("/api/rescue-orders")
public class RescueOrderController {

    private final RescueOrderService
            rescueOrderService;

    public RescueOrderController(
            RescueOrderService rescueOrderService
    ) {
        this.rescueOrderService =
                rescueOrderService;
    }

    @PostMapping
    public ApiResponse<RescueOrderResponse>
    createOrder(
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody
            CreateRescueOrderRequest request
    ) {
        Long reporterId =
                Long.valueOf(jwt.getSubject());

        RescueOrderResponse response =
                rescueOrderService.createOrder(
                        reporterId,
                        request
                );

        return ApiResponse.success(response);
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
        Long reporterId =
                Long.valueOf(jwt.getSubject());

        PageResponse<RescueOrderResponse> response =
                rescueOrderService.getMyOrders(
                        reporterId,
                        page,
                        size
                );

        return ApiResponse.success(response);
    }

    @GetMapping("/public")
    public ApiResponse<PageResponse<PublicRescueOrderResponse>>
    getPublicOrders(
            @RequestParam(defaultValue = "1")
            long page,

            @RequestParam(defaultValue = "10")
            long size
    ) {
        PageResponse<PublicRescueOrderResponse> response =
                rescueOrderService.getPublicOrders(
                        page,
                        size
                );

        return ApiResponse.success(response);
    }
}