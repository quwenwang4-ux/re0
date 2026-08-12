package com.seafish.controller.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class ReviewVolunteerApplicationRequest {

    @NotBlank(message = "审批结果不能为空")
    @Pattern(
            regexp = "^(APPROVED|REJECTED)$",
            message = "审批结果只能是APPROVED或REJECTED"
    )
    private String status;

    @Size(max = 500, message = "审批意见不能超过500个字符")
    private String reviewComment;

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getReviewComment() {
        return reviewComment;
    }

    public void setReviewComment(
            String reviewComment
    ) {
        this.reviewComment = reviewComment;
    }
}