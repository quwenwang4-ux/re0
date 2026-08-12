package com.seafish.controller.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class ReviewRescueOrderRequest {

    @NotBlank(message = "审核决定不能为空")
    @Pattern(
            regexp = "^(APPROVED|REJECTED)$",
            message = "审核决定只能是APPROVED或REJECTED"
    )
    private String decision;

    @Size(max = 500, message = "审核意见不能超过500个字符")
    private String reviewComment;

    public String getDecision() {
        return decision;
    }

    public void setDecision(String decision) {
        this.decision = decision;
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