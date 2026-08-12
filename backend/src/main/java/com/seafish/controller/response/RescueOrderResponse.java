package com.seafish.controller.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public class RescueOrderResponse {

    private final Long id;

    private final Long reporterId;

    private final String issueType;

    private final String title;

    private final String description;

    private final String locationText;

    private final BigDecimal latitude;

    private final BigDecimal longitude;

    private final String status;

    private final Long reviewerId;

    private final String reviewComment;

    private final LocalDateTime reviewedAt;

    private final LocalDateTime publishedAt;

    private final Long volunteerId;

    private final LocalDateTime acceptedAt;

    private final String completionDescription;

    private final LocalDateTime completionSubmittedAt;

    private final Long confirmerId;

    private final String confirmComment;

    private final LocalDateTime confirmedAt;

    private final LocalDateTime createdAt;

    private final LocalDateTime updatedAt;

    private final List<RescueOrderImageResponse> images;

    public Long getId() {
        return id;
    }

    public Long getReporterId() {
        return reporterId;
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

    public Long getReviewerId() {
        return reviewerId;
    }

    public String getReviewComment() {
        return reviewComment;
    }

    public LocalDateTime getReviewedAt() {
        return reviewedAt;
    }

    public LocalDateTime getPublishedAt() {
        return publishedAt;
    }

    public Long getVolunteerId() {
        return volunteerId;
    }

    public LocalDateTime getAcceptedAt() {
        return acceptedAt;
    }

    public String getCompletionDescription() {
        return completionDescription;
    }

    public LocalDateTime getCompletionSubmittedAt() {
        return completionSubmittedAt;
    }

    public Long getConfirmerId() {
        return confirmerId;
    }

    public String getConfirmComment() {
        return confirmComment;
    }

    public LocalDateTime getConfirmedAt() {
        return confirmedAt;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public List<RescueOrderImageResponse> getImages() {
        return images;
    }

    public RescueOrderResponse(Long id,
                               Long reporterId,
                               String issueType,
                               String title,
                               String description,
                               String locationText,
                               BigDecimal latitude,
                               BigDecimal longitude,
                               String status,
                               Long reviewerId,
                               String reviewComment,
                               LocalDateTime reviewedAt,
                               LocalDateTime publishedAt,
                               Long volunteerId,
                               LocalDateTime acceptedAt,
                               String completionDescription,
                               LocalDateTime completionSubmittedAt,
                               Long confirmerId,
                               String confirmComment,
                               LocalDateTime confirmedAt,
                               LocalDateTime createdAt,
                               LocalDateTime updatedAt,
                               List<RescueOrderImageResponse> images) {
        this.id = id;
        this.reporterId = reporterId;
        this.issueType = issueType;
        this.title = title;
        this.description = description;
        this.locationText = locationText;
        this.latitude = latitude;
        this.longitude = longitude;
        this.status = status;
        this.reviewerId = reviewerId;
        this.reviewComment = reviewComment;
        this.reviewedAt = reviewedAt;
        this.publishedAt = publishedAt;
        this.volunteerId = volunteerId;
        this.acceptedAt = acceptedAt;
        this.completionDescription = completionDescription;
        this.completionSubmittedAt = completionSubmittedAt;
        this.confirmerId = confirmerId;
        this.confirmComment = confirmComment;
        this.confirmedAt = confirmedAt;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.images = images;

    }
}