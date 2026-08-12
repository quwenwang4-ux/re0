package com.seafish.controller.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.List;

public class CompleteRescueOrderRequest {

    @NotBlank(message = "完成情况说明不能为空")
    @Size(
            max = 5000,
            message = "完成情况说明不能超过5000个字符"
    )
    private String completionDescription;

    @Size(
            max = 9,
            message = "完成反馈图片不能超过9张"
    )
    private List<
            @NotBlank(message = "图片地址不能为空")
            @Size(
                    max = 500,
                    message = "图片地址不能超过500个字符"
            )
                    String
            > imageUrls;

    public String getCompletionDescription() {
        return completionDescription;
    }

    public void setCompletionDescription(
            String completionDescription
    ) {
        this.completionDescription =
                completionDescription;
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