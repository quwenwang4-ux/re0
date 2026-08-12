package com.seafish.controller.response;

import java.time.LocalDateTime;

public class RescueOrderImageResponse {

    private final Long id;

    private final String imageType;

    private final String imageUrl;

    private final Long uploadedBy;

    private final Integer sortOrder;

    private final LocalDateTime createdAt;

    public RescueOrderImageResponse(
            Long id,
            String imageType,
            String imageUrl,
            Long uploadedBy,
            Integer sortOrder,
            LocalDateTime createdAt
    ) {
        this.id = id;
        this.imageType = imageType;
        this.imageUrl = imageUrl;
        this.uploadedBy = uploadedBy;
        this.sortOrder = sortOrder;
        this.createdAt = createdAt;
    }

    public Long getId() {
        return id;
    }

    public String getImageType() {
        return imageType;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public Long getUploadedBy() {
        return uploadedBy;
    }

    public Integer getSortOrder() {
        return sortOrder;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}