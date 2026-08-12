package com.seafish.controller.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class ReviewFishProfileTaskRequest {
    @NotBlank(message = "处理决定不能为空")
    @Pattern(regexp = "^(RESOLVED|IGNORED)$", message = "处理决定只能是RESOLVED或IGNORED")
    private String decision;
    private Long fishId;
    @Size(max = 500, message = "处理意见不能超过500个字符")
    private String reviewComment;

    public String getDecision() { return decision; }
    public void setDecision(String decision) { this.decision = decision; }
    public Long getFishId() { return fishId; }
    public void setFishId(Long fishId) { this.fishId = fishId; }
    public String getReviewComment() { return reviewComment; }
    public void setReviewComment(String reviewComment) { this.reviewComment = reviewComment; }
}
