package com.seafish.controller.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public class UpdateUserStatusRequest {
    @NotBlank(message = "用户状态不能为空")
    @Pattern(regexp = "^(ACTIVE|DISABLED)$", message = "用户状态只能是ACTIVE或DISABLED")
    private String status;

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
