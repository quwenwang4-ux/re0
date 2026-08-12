package com.seafish.controller.request;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.util.List;

public class CreateRescueOrderRequest {

    @NotBlank(message = "问题类型不能为空")
    @Pattern(
            regexp = "^(INJURED_ANIMAL|ENVIRONMENT|OTHER)$",
            message = "问题类型不合法"
    )
    private String issueType;

    @NotBlank(message = "工单标题不能为空")
    @Size(max = 150, message = "工单标题不能超过150个字符")
    private String title;

    @NotBlank(message = "问题描述不能为空")
    @Size(max = 5000, message = "问题描述不能超过5000个字符")
    private String description;

    @Size(max = 255, message = "位置描述不能超过255个字符")
    private String locationText;

    @DecimalMin(
            value = "-90.0",
            message = "纬度不能小于-90"
    )
    @DecimalMax(
            value = "90.0",
            message = "纬度不能大于90"
    )
    @Digits(
            integer = 3,
            fraction = 7,
            message = "纬度最多保留7位小数"
    )
    private BigDecimal latitude;

    @DecimalMin(
            value = "-180.0",
            message = "经度不能小于-180"
    )
    @DecimalMax(
            value = "180.0",
            message = "经度不能大于180"
    )
    @Digits(
            integer = 3,
            fraction = 7,
            message = "经度最多保留7位小数"
    )
    private BigDecimal longitude;

    @Size(max = 9, message = "工单图片不能超过9张")
    private List<
            @NotBlank(message = "图片地址不能为空")
            @Size(
                    max = 500,
                    message = "图片地址不能超过500个字符"
            )
                    String
            > imageUrls;

    public String getIssueType() {
        return issueType;
    }

    public void setIssueType(String issueType) {
        this.issueType = issueType;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(
            String description
    ) {
        this.description = description;
    }

    public String getLocationText() {
        return locationText;
    }

    public void setLocationText(
            String locationText
    ) {
        this.locationText = locationText;
    }

    public BigDecimal getLatitude() {
        return latitude;
    }

    public void setLatitude(
            BigDecimal latitude
    ) {
        this.latitude = latitude;
    }

    public BigDecimal getLongitude() {
        return longitude;
    }

    public void setLongitude(
            BigDecimal longitude
    ) {
        this.longitude = longitude;
    }

    public List<String> getImageUrls() {
        return imageUrls;
    }

    public void setImageUrls(
            List<String> imageUrls
    ) {
        this.imageUrls = imageUrls;
    }
}