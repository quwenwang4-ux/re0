package com.seafish.controller;

import com.seafish.common.ApiResponse;
import com.seafish.controller.response.CurrentUserResponse;
import com.seafish.service.UserService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(
            UserService userService
    ) {
        this.userService = userService;
    }

    @GetMapping("/me")
    public ApiResponse<CurrentUserResponse> getMe(
            @AuthenticationPrincipal Jwt jwt
    ) {
        Long userId = Long.valueOf(
                jwt.getSubject()
        );

        CurrentUserResponse response =
                userService.getCurrentUser(userId);

        return ApiResponse.success(response);
    }
}
