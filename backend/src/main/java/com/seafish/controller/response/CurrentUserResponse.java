package com.seafish.controller.response;

import java.util.List;

public class CurrentUserResponse {

    private final UserResponse user;
    private final List<String> roles;

    public CurrentUserResponse(
            UserResponse user,
            List<String> roles
    ) {
        this.user = user;
        this.roles = roles;
    }

    public UserResponse getUser() {
        return user;
    }

    public List<String> getRoles() {
        return roles;
    }
}
