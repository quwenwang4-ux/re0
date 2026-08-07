package com.seafish.controller.response;

import java.time.LocalDateTime;

public class UserResponse {

    private final Long id;
    private final String username;
    private final String nickname;
    private final String email;
    private final String phone;
    private final String avatarUrl;
    private final String status;
    private final LocalDateTime createdAt;

    public UserResponse(
            Long id,
            String username,
            String nickname,
            String email,
            String phone,
            String avatarUrl,
            String status,
            LocalDateTime createdAt
    ) {
        this.id = id;
        this.username = username;
        this.nickname = nickname;
        this.email = email;
        this.phone = phone;
        this.avatarUrl = avatarUrl;
        this.status = status;
        this.createdAt = createdAt;
    }

    public Long getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getNickname() {
        return nickname;
    }

    public String getEmail() {
        return email;
    }

    public String getPhone() {
        return phone;
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }

    public String getStatus() {
        return status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}