CREATE TABLE volunteer_application (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键',
    applicant_id BIGINT NOT NULL COMMENT '申请用户编号',
    real_name VARCHAR(100) NOT NULL COMMENT '真实姓名',
    phone VARCHAR(30) NOT NULL COMMENT '联系电话',
    region VARCHAR(100) NOT NULL COMMENT '所在地区',
    skills VARCHAR(500) NULL COMMENT '擅长领域',
    reason TEXT NOT NULL COMMENT '申请理由',
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING' COMMENT '申请状态',
    reviewer_id BIGINT NULL COMMENT '审核管理员编号',
    review_comment VARCHAR(500) NULL COMMENT '审核意见',
    reviewed_at DATETIME NULL COMMENT '审核时间',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
        ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    CONSTRAINT ck_volunteer_application_status
        CHECK (status IN ('PENDING', 'APPROVED', 'REJECTED', 'CANCELLED')),
    CONSTRAINT fk_volunteer_application_applicant
        FOREIGN KEY (applicant_id) REFERENCES sys_user (id),
    CONSTRAINT fk_volunteer_application_reviewer
        FOREIGN KEY (reviewer_id) REFERENCES sys_user (id),
    INDEX idx_volunteer_application_applicant_status (applicant_id, status),
    INDEX idx_volunteer_application_status (status)
) ENGINE = InnoDB
  DEFAULT CHARACTER SET = utf8mb4
  COLLATE = utf8mb4_unicode_ci
  COMMENT = '志愿者申请表';
