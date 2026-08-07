package com.seafish.controller.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class RegisterRequest {

    @NotBlank(message = "用户名不能为空")
    @Size(
            min = 4,
            max = 50,
            message = "用户名长度必须在 4 到 50 个字符之间"
    )
    @Pattern(
            regexp = "^[a-zA-Z0-9_]+$",
            message = "用户名只能包含字母、数字和下划线"
    )
    private String username;

    @NotBlank(message = "密码不能为空")
    @Size(
            min = 8,
            max = 64,
            message = "密码长度必须在 8 到 64 个字符之间"
    )
    private String password;

    @Size(
            max = 100,
            message = "昵称不能超过 100 个字符"
    )
    private String nickname;

    @Email(message = "邮箱格式不正确")
    @Size(
            max = 255,
            message = "邮箱不能超过 255 个字符"
    )
    private String email;

    @Pattern(
            regexp = "^$|^1\\d{10}$",
            message = "手机号格式不正确"
    )
    private String phone;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }
}