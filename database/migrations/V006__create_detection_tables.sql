CREATE TABLE detection_record (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '识别记录编号',
    user_id BIGINT NOT NULL COMMENT '发起识别的用户编号',
    original_image_url VARCHAR(500) NOT NULL COMMENT '原始图片存储地址',
    original_file_name VARCHAR(255) NULL COMMENT '用户上传时的文件名',
    result_image_url VARCHAR(500) NULL COMMENT '标注结果图片地址',
    model_name VARCHAR(100) NOT NULL COMMENT '模型名称或版本',
    confidence_threshold DECIMAL(5,4) NOT NULL COMMENT '置信度阈值',
    status VARCHAR(30) NOT NULL COMMENT 'PROCESSING、SUCCESS、NO_TARGET、FAILED',
    error_code VARCHAR(50) NULL COMMENT '失败类型编码',
    error_message VARCHAR(500) NULL COMMENT '对用户可理解的失败原因',
    started_at DATETIME NOT NULL COMMENT '识别开始时间',
    completed_at DATETIME NULL COMMENT '识别完成时间',
    duration_ms BIGINT NULL COMMENT '识别耗时，单位毫秒',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '记录创建时间',

    CONSTRAINT fk_detection_record_user
        FOREIGN KEY (user_id) REFERENCES sys_user(id),
    CONSTRAINT ck_detection_record_status
        CHECK (status IN ('PROCESSING', 'SUCCESS', 'NO_TARGET', 'FAILED')),

    INDEX idx_detection_record_user_created (user_id, created_at),
    INDEX idx_detection_record_status_created (status, created_at)
) ENGINE = InnoDB
  DEFAULT CHARACTER SET = utf8mb4
  COLLATE = utf8mb4_unicode_ci
  COMMENT = '鱼类图片识别记录表';

CREATE TABLE detection_result (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '识别结果编号',
    record_id BIGINT NOT NULL COMMENT '所属识别记录编号',
    fish_id BIGINT NULL COMMENT '匹配到的鱼类资料编号',
    class_name VARCHAR(150) NOT NULL COMMENT '模型返回的类别名称',
    confidence DECIMAL(5,4) NOT NULL COMMENT '置信度',
    box_x1 DECIMAL(10,4) NOT NULL COMMENT '目标框左上角横坐标',
    box_y1 DECIMAL(10,4) NOT NULL COMMENT '目标框左上角纵坐标',
    box_x2 DECIMAL(10,4) NOT NULL COMMENT '目标框右下角横坐标',
    box_y2 DECIMAL(10,4) NOT NULL COMMENT '目标框右下角纵坐标',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',

    CONSTRAINT fk_detection_result_record
        FOREIGN KEY (record_id) REFERENCES detection_record(id),
    CONSTRAINT fk_detection_result_fish
        FOREIGN KEY (fish_id) REFERENCES fish_info(id),

    INDEX idx_detection_result_record (record_id),
    INDEX idx_detection_result_fish (fish_id),
    INDEX idx_detection_result_class (class_name)
) ENGINE = InnoDB
  DEFAULT CHARACTER SET = utf8mb4
  COLLATE = utf8mb4_unicode_ci
  COMMENT = '鱼类图片识别结果明细表';

CREATE TABLE fish_profile_task (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '资料补全任务编号',
    detection_result_id BIGINT NOT NULL COMMENT '触发任务的识别结果编号',
    class_name VARCHAR(150) NOT NULL COMMENT '模型识别类别',
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING' COMMENT 'PENDING、RESOLVED、IGNORED',
    linked_fish_id BIGINT NULL COMMENT '管理员关联的正式鱼类编号',
    reviewer_id BIGINT NULL COMMENT '处理管理员编号',
    review_comment VARCHAR(500) NULL COMMENT '处理意见',
    reviewed_at DATETIME NULL COMMENT '处理时间',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',

    CONSTRAINT uk_fish_profile_task_result UNIQUE (detection_result_id),
    CONSTRAINT fk_fish_profile_task_result
        FOREIGN KEY (detection_result_id) REFERENCES detection_result(id),
    CONSTRAINT fk_fish_profile_task_fish
        FOREIGN KEY (linked_fish_id) REFERENCES fish_info(id),
    CONSTRAINT fk_fish_profile_task_reviewer
        FOREIGN KEY (reviewer_id) REFERENCES sys_user(id),
    CONSTRAINT ck_fish_profile_task_status
        CHECK (status IN ('PENDING', 'RESOLVED', 'IGNORED')),

    INDEX idx_fish_profile_task_status (status, created_at),
    INDEX idx_fish_profile_task_class (class_name)
) ENGINE = InnoDB
  DEFAULT CHARACTER SET = utf8mb4
  COLLATE = utf8mb4_unicode_ci
  COMMENT = '识别结果对应的鱼类资料补全任务表';
