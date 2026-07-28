package com.seafish.controller;

import com.seafish.common.ApiResponse;
import com.seafish.controller.response.HealthResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class HealthController {

    @GetMapping("/health")
    public ApiResponse<HealthResponse> health() {
        HealthResponse healthResponse = new HealthResponse(
                "UP",
                "seafish-backend",
                "0.3.0-dev"
        );

        return ApiResponse.success(healthResponse);
    }

    @GetMapping("/version")
    public ApiResponse<String> version() {
        return ApiResponse.success("0.3.0-dev");
    }


}
