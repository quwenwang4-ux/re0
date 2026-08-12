package com.seafish.controller.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class ConfirmRescueCompletionRequest {

    @NotBlank(message = "确认决定不能为空")
    @Pattern(
            regexp = "^(CONFIRMED|REJECTED)$",
            message = "确认决定只能是CONFIRMED或REJECTED"
    )
    private String decision;

    @Size(max = 500, message = "确认意见不能超过500个字符")
    private String confirmComment;

    public String getDecision() {
        return decision;
    }

    public void setDecision(String decision) {
        this.decision = decision;
    }

    public String getConfirmComment() {
        return confirmComment;
    }

    public void setConfirmComment(
            String confirmComment
    ) {
        this.confirmComment = confirmComment;
    }
}
