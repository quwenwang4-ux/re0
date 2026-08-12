CREATE TABLE fish_info_application (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '申请编号',
    applicant_id BIGINT NOT NULL COMMENT '申请用户编号',
    application_type VARCHAR(20) NOT NULL COMMENT 'ADD或CORRECTION',
    target_fish_id BIGINT NULL COMMENT '纠错目标鱼类编号',
    chinese_name VARCHAR(100) NOT NULL COMMENT '建议中文名称',
    scientific_name VARCHAR(150) NULL COMMENT '建议学名',
    category VARCHAR(100) NULL COMMENT '建议类别',
    appearance TEXT NULL COMMENT '建议外观特征',
    habits TEXT NULL COMMENT '建议生活习性',
    habitat TEXT NULL COMMENT '建议栖息环境',
    distribution TEXT NULL COMMENT '建议分布区域',
    protection_level VARCHAR(50) NULL COMMENT '建议保护级别',
    cover_image_url VARCHAR(500) NULL COMMENT '建议封面图片地址',
    source_description VARCHAR(500) NOT NULL COMMENT '资料来源说明',
    reason VARCHAR(1000) NOT NULL COMMENT '申请原因',
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING' COMMENT '申请状态',
    reviewer_id BIGINT NULL COMMENT '审核管理员编号',
    review_comment VARCHAR(500) NULL COMMENT '审核意见',
    reviewed_at DATETIME NULL COMMENT '审核时间',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
        ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',

    CONSTRAINT fk_fish_application_applicant
        FOREIGN KEY (applicant_id) REFERENCES sys_user(id),
    CONSTRAINT fk_fish_application_target
        FOREIGN KEY (target_fish_id) REFERENCES fish_info(id),
    CONSTRAINT fk_fish_application_reviewer
        FOREIGN KEY (reviewer_id) REFERENCES sys_user(id),
    CONSTRAINT ck_fish_application_type
        CHECK (application_type IN ('ADD', 'CORRECTION')),
    CONSTRAINT ck_fish_application_status
        CHECK (status IN ('PENDING', 'APPROVED', 'REJECTED')),

    INDEX idx_fish_application_applicant (applicant_id, created_at),
    INDEX idx_fish_application_status (status, created_at)
) ENGINE = InnoDB
  DEFAULT CHARACTER SET = utf8mb4
  COLLATE = utf8mb4_unicode_ci
  COMMENT = '鱼类资料新增与纠错申请表';
