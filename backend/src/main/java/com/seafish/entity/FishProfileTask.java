package com.seafish.entity;

import com.baomidou.mybatisplus.annotation.FieldStrategy;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.time.LocalDateTime;

@TableName("fish_profile_task")
public class FishProfileTask {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long detectionResultId;
    private String className;
    private String status;
    private Long linkedFishId;
    private Long reviewerId;
    private String reviewComment;
    private LocalDateTime reviewedAt;
    @TableField(insertStrategy = FieldStrategy.NEVER, updateStrategy = FieldStrategy.NEVER)
    private LocalDateTime createdAt;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getDetectionResultId() { return detectionResultId; }
    public void setDetectionResultId(Long detectionResultId) { this.detectionResultId = detectionResultId; }
    public String getClassName() { return className; }
    public void setClassName(String className) { this.className = className; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public Long getLinkedFishId() { return linkedFishId; }
    public void setLinkedFishId(Long linkedFishId) { this.linkedFishId = linkedFishId; }
    public Long getReviewerId() { return reviewerId; }
    public void setReviewerId(Long reviewerId) { this.reviewerId = reviewerId; }
    public String getReviewComment() { return reviewComment; }
    public void setReviewComment(String reviewComment) { this.reviewComment = reviewComment; }
    public LocalDateTime getReviewedAt() { return reviewedAt; }
    public void setReviewedAt(LocalDateTime reviewedAt) { this.reviewedAt = reviewedAt; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
