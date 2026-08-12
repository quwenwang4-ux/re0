package com.seafish.controller;

import com.seafish.common.ApiResponse;
import com.seafish.controller.response.CurrentUserResponse;
import com.seafish.service.UserService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.seafish.controller.request.UpdateProfileRequest;
import com.seafish.controller.response.UserResponse;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;

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

    @PutMapping("/me")
    public ApiResponse<UserResponse> updateMe(
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody
            UpdateProfileRequest request
    ) {
        Long userId = Long.valueOf(
                jwt.getSubject()
        );

        UserResponse response =
                userService.updateCurrentUser(
                        userId,
                        request
                );

        return ApiResponse.success(response);
    }
}
