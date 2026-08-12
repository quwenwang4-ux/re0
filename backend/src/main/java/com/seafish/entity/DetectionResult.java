package com.seafish.entity;

import com.baomidou.mybatisplus.annotation.FieldStrategy;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@TableName("detection_result")
public class DetectionResult {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long recordId;
    private Long fishId;
    private String className;
    private BigDecimal confidence;
    private BigDecimal boxX1;
    private BigDecimal boxY1;
    private BigDecimal boxX2;
    private BigDecimal boxY2;

    @TableField(insertStrategy = FieldStrategy.NEVER, updateStrategy = FieldStrategy.NEVER)
    private LocalDateTime createdAt;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getRecordId() { return recordId; }
    public void setRecordId(Long recordId) { this.recordId = recordId; }
    public Long getFishId() { return fishId; }
    public void setFishId(Long fishId) { this.fishId = fishId; }
    public String getClassName() { return className; }
    public void setClassName(String className) { this.className = className; }
    public BigDecimal getConfidence() { return confidence; }
    public void setConfidence(BigDecimal confidence) { this.confidence = confidence; }
    public BigDecimal getBoxX1() { return boxX1; }
    public void setBoxX1(BigDecimal boxX1) { this.boxX1 = boxX1; }
    public BigDecimal getBoxY1() { return boxY1; }
    public void setBoxY1(BigDecimal boxY1) { this.boxY1 = boxY1; }
    public BigDecimal getBoxX2() { return boxX2; }
    public void setBoxX2(BigDecimal boxX2) { this.boxX2 = boxX2; }
    public BigDecimal getBoxY2() { return boxY2; }
    public void setBoxY2(BigDecimal boxY2) { this.boxY2 = boxY2; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
