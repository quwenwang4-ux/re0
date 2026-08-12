package com.seafish.controller.response;

import java.time.LocalDateTime;

public class VolunteerApplicationResponse {

    private final Long id;

    private final Long applicantId;

    private final String realName;

    private final String phone;

    private final String region;

    private final String skills;

    private final String reason;

    private final String status;

    private final Long reviewerId;

    private final String reviewComment;

    private final LocalDateTime reviewedAt;

    private final LocalDateTime createdAt;

    private final LocalDateTime updatedAt;

    public VolunteerApplicationResponse(
            Long id,
            Long applicantId,
            String realName,
            String phone,
            String region,
            String skills,
            String reason,
            String status,
            Long reviewerId,
            String reviewComment,
            LocalDateTime reviewedAt,
            LocalDateTime createdAt,
            LocalDateTime updatedAt
    ) {
        this.id = id;
        this.applicantId = applicantId;
        this.realName = realName;
        this.phone = phone;
        this.region = region;
        this.skills = skills;
        this.reason = reason;
        this.status = status;
        this.reviewerId = reviewerId;
        this.reviewComment = reviewComment;
        this.reviewedAt = reviewedAt;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public Long getId() {
        return id;
    }

    public Long getApplicantId() {
        return applicantId;
    }

    public String getRealName() {
        return realName;
    }

    public String getPhone() {
        return phone;
    }

    public String getRegion() {
        return region;
    }

    public String getSkills() {
        return skills;
    }

    public String getReason() {
        return reason;
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

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
}