package com.seafish.controller.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class CreateVolunteerApplicationRequest {

    @NotBlank(message = "真实姓名不能为空")
    @Size(max = 100, message = "真实姓名不能超过100个字符")
    private String realName;

    @NotBlank(message = "联系电话不能为空")
    @Pattern(
            regexp = "^1\\d{10}$",
            message = "联系电话格式不正确"
    )
    private String phone;

    @NotBlank(message = "所在地区不能为空")
    @Size(max = 100, message = "所在地区不能超过100个字符")
    private String region;

    @Size(max = 500, message = "擅长领域不能超过500个字符")
    private String skills;

    @NotBlank(message = "申请理由不能为空")
    @Size(max = 2000, message = "申请理由不能超过2000个字符")
    private String reason;

    public String getRealName() {
        return realName;
    }

    public void setRealName(String realName) {
        this.realName = realName;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public String getSkills() {
        return skills;
    }

    public void setSkills(String skills) {
        this.skills = skills;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }
}