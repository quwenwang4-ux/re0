package com.seafish.controller;

import com.seafish.controller.response.HealthResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class HealthController {

    @GetMapping("/health")
    public HealthResponse health() {
        return new HealthResponse(
                "UP",
                "seafish-backend",
                "0.3.0-dev"
        );
    }

    @GetMapping("/version")
    public String version() {
        return "0.3.0-dev";
    }
}
