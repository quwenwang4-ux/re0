CREATE TABLE fish_info (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键',
    chinese_name VARCHAR(100) NOT NULL COMMENT '中文名称',
    scientific_name VARCHAR(150) NULL COMMENT '学名',
    category VARCHAR(100) NULL COMMENT '鱼类类别',
    appearance TEXT NULL COMMENT '外观特征',
    habits TEXT NULL COMMENT '生活习性',
    habitat TEXT NULL COMMENT '栖息环境',
    distribution TEXT NULL COMMENT '分布区域',
    protection_level VARCHAR(50) NULL COMMENT '保护级别',
    cover_image_url VARCHAR(500) NULL COMMENT '封面图片地址',
    source_type VARCHAR(30) NOT NULL COMMENT '数据来源类型',
    source_description VARCHAR(500) NULL COMMENT '来源说明',
    created_by BIGINT NULL COMMENT '创建人编号',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
        ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除标记',
    CONSTRAINT uk_fish_info_scientific_name UNIQUE (scientific_name),
    INDEX idx_fish_info_chinese_name (chinese_name),
    INDEX idx_fish_info_category (category),
    INDEX idx_fish_info_deleted (deleted)
) ENGINE = InnoDB
  DEFAULT CHARACTER SET = utf8mb4
  COLLATE = utf8mb4_unicode_ci
  COMMENT = '海洋鱼类信息表';
