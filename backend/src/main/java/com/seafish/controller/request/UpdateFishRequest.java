package com.seafish.controller.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class UpdateFishRequest {

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

    @NotBlank(message = "数据来源类型不能为空")
    @Size(max = 30, message = "数据来源类型不能超过30个字符")
    private String sourceType;

    @Size(max = 500, message = "数据来源说明不能超过500个字符")
    private String sourceDescription;

    public String getChineseName() {
        return chineseName;
    }

    public void setChineseName(String chineseName) {
        this.chineseName = chineseName;
    }

    public String getScientificName() {
        return scientificName;
    }

    public void setScientificName(String scientificName) {
        this.scientificName = scientificName;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getAppearance() {
        return appearance;
    }

    public void setAppearance(String appearance) {
        this.appearance = appearance;
    }

    public String getHabits() {
        return habits;
    }

    public void setHabits(String habits) {
        this.habits = habits;
    }

    public String getHabitat() {
        return habitat;
    }

    public void setHabitat(String habitat) {
        this.habitat = habitat;
    }

    public String getDistribution() {
        return distribution;
    }

    public void setDistribution(String distribution) {
        this.distribution = distribution;
    }

    public String getProtectionLevel() {
        return protectionLevel;
    }

    public void setProtectionLevel(String protectionLevel) {
        this.protectionLevel = protectionLevel;
    }

    public String getCoverImageUrl() {
        return coverImageUrl;
    }

    public void setCoverImageUrl(String coverImageUrl) {
        this.coverImageUrl = coverImageUrl;
    }

    public String getSourceType() {
        return sourceType;
    }

    public void setSourceType(String sourceType) {
        this.sourceType = sourceType;
    }

    public String getSourceDescription() {
        return sourceDescription;
    }

    public void setSourceDescription(String sourceDescription) {
        this.sourceDescription = sourceDescription;
    }
}
