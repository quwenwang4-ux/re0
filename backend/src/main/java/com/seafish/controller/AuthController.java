package com.seafish.controller;

import com.seafish.common.ApiResponse;
import com.seafish.controller.request.RegisterRequest;
import com.seafish.controller.response.UserResponse;
import com.seafish.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserService userService;

    public AuthController(
            UserService userService
    ) {
        this.userService = userService;
    }

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<UserResponse> register(
            @Valid
            @RequestBody
            RegisterRequest request
    ) {
        UserResponse response =
                userService.register(request);

        return ApiResponse.success(response);
    }
}