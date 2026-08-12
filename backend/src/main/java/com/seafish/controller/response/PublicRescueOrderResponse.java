package com.seafish.controller.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public class PublicRescueOrderResponse {

    private final Long id;

    private final String issueType;

    private final String title;

    private final String description;

    private final String locationText;

    private final BigDecimal latitude;

    private final BigDecimal longitude;

    private final String status;

    private final LocalDateTime publishedAt;

    private final List<String> imageUrls;

    public PublicRescueOrderResponse(
            Long id,
            String issueType,
            String title,
            String description,
            String locationText,
            BigDecimal latitude,
            BigDecimal longitude,
            String status,
            LocalDateTime publishedAt,
            List<String> imageUrls
    ) {
        this.id = id;
        this.issueType = issueType;
        this.title = title;
        this.description = description;
        this.locationText = locationText;
        this.latitude = latitude;
        this.longitude = longitude;
        this.status = status;
        this.publishedAt = publishedAt;
        this.imageUrls = imageUrls;
    }

    public Long getId() {
        return id;
    }

    public String getIssueType() {
        return issueType;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public String getLocationText() {
        return locationText;
    }

    public BigDecimal getLatitude() {
        return latitude;
    }

    public BigDecimal getLongitude() {
        return longitude;
    }

    public String getStatus() {
        return status;
    }

    public LocalDateTime getPublishedAt() {
        return publishedAt;
    }

    public List<String> getImageUrls() {
        return imageUrls;
    }
}