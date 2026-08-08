package com.seafish.controller.response;

import java.util.List;

public class LoginResponse {

    private final String accessToken;
    private final String tokenType;
    private final long expiresInSeconds;
    private final UserResponse user;
    private final List<String> roles;

    public LoginResponse(
            String accessToken,
            String tokenType,
            long expiresInSeconds,
            UserResponse user,
            List<String> roles
    ) {
        this.accessToken = accessToken;
        this.tokenType = tokenType;
        this.expiresInSeconds = expiresInSeconds;
        this.user = user;
        this.roles = roles;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public String getTokenType() {
        return tokenType;
    }

    public long getExpiresInSeconds() {
        return expiresInSeconds;
    }

    public UserResponse getUser() {
        return user;
    }

    public List<String> getRoles() {
        return roles;
    }
}
