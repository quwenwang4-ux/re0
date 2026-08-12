package com.seafish.controller.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class CreateFishInfoApplicationRequest {
    @NotBlank(message = "申请类型不能为空")
    @Pattern(regexp = "^(ADD|CORRECTION)$", message = "申请类型只能是ADD或CORRECTION")
    private String applicationType;
    private Long targetFishId;
    @NotBlank(message = "鱼类中文名不能为空")
    @Size(max = 100, message = "鱼类中文名不能超过100个字符")
    private String chineseName;
    @Size(max = 150, message = "鱼类学名不能超过150个字符")
    private String scientificName;
    @Size(max = 100, message = "鱼类类别不能超过100个字符")
    private String category;
    private String appearance;
    private String habits;
    private String habitat;
    private String distribution;
    @Size(max = 50, message = "保护级别不能超过50个字符")
    private String protectionLevel;
    @Size(max = 500, message = "封面图片地址不能超过500个字符")
    private String coverImageUrl;
    @NotBlank(message = "资料来源说明不能为空")
    @Size(max = 500, message = "资料来源说明不能超过500个字符")
    private String sourceDescription;
    @NotBlank(message = "申请原因不能为空")
    @Size(max = 1000, message = "申请原因不能超过1000个字符")
    private String reason;

    public String getApplicationType() { return applicationType; }
    public void setApplicationType(String applicationType) { this.applicationType = applicationType; }
    public Long getTargetFishId() { return targetFishId; }
    public void setTargetFishId(Long targetFishId) { this.targetFishId = targetFishId; }
    public String getChineseName() { return chineseName; }
    public void setChineseName(String chineseName) { this.chineseName = chineseName; }
    public String getScientificName() { return scientificName; }
    public void setScientificName(String scientificName) { this.scientificName = scientificName; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public String getAppearance() { return appearance; }
    public void setAppearance(String appearance) { this.appearance = appearance; }
    public String getHabits() { return habits; }
    public void setHabits(String habits) { this.habits = habits; }
    public String getHabitat() { return habitat; }
    public void setHabitat(String habitat) { this.habitat = habitat; }
    public String getDistribution() { return distribution; }
    public void setDistribution(String distribution) { this.distribution = distribution; }
    public String getProtectionLevel() { return protectionLevel; }
    public void setProtectionLevel(String protectionLevel) { this.protectionLevel = protectionLevel; }
    public String getCoverImageUrl() { return coverImageUrl; }
    public void setCoverImageUrl(String coverImageUrl) { this.coverImageUrl = coverImageUrl; }
    public String getSourceDescription() { return sourceDescription; }
    public void setSourceDescription(String sourceDescription) { this.sourceDescription = sourceDescription; }
    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
}
