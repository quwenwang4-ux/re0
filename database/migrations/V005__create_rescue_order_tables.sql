CREATE TABLE rescue_order (
                              id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '工单主键',

                              reporter_id BIGINT NOT NULL COMMENT '上报用户编号',

                              issue_type VARCHAR(30) NOT NULL COMMENT '问题类型',
                              title VARCHAR(150) NOT NULL COMMENT '工单标题',
                              description TEXT NOT NULL COMMENT '问题描述',

                              location_text VARCHAR(255) NULL COMMENT '手动填写的位置',
                              latitude DECIMAL(10, 7) NULL COMMENT '纬度',
                              longitude DECIMAL(10, 7) NULL COMMENT '经度',

                              status VARCHAR(30) NOT NULL
                                  DEFAULT 'PENDING_REVIEW'
                                  COMMENT '工单状态',

                              reviewer_id BIGINT NULL COMMENT '初审管理员编号',
                              review_comment VARCHAR(500) NULL COMMENT '初审意见',
                              reviewed_at DATETIME NULL COMMENT '初审时间',
                              published_at DATETIME NULL COMMENT '公告发布时间',

                              volunteer_id BIGINT NULL COMMENT '接单志愿者编号',
                              accepted_at DATETIME NULL COMMENT '接单时间',

                              completion_description TEXT NULL COMMENT '完成反馈说明',
                              completion_submitted_at DATETIME NULL
        COMMENT '完成反馈提交时间',

                              confirmer_id BIGINT NULL COMMENT '最终确认管理员编号',
                              confirm_comment VARCHAR(500) NULL COMMENT '最终确认意见',
                              confirmed_at DATETIME NULL COMMENT '最终确认时间',

                              created_at DATETIME NOT NULL
                                  DEFAULT CURRENT_TIMESTAMP
                                  COMMENT '创建时间',

                              updated_at DATETIME NOT NULL
                                  DEFAULT CURRENT_TIMESTAMP
                                  ON UPDATE CURRENT_TIMESTAMP
        COMMENT '更新时间',

                              CONSTRAINT ck_rescue_order_issue_type
                                  CHECK (
                                      issue_type IN (
                                                     'INJURED_ANIMAL',
                                                     'ENVIRONMENT',
                                                     'OTHER'
                                          )
                                      ),

                              CONSTRAINT ck_rescue_order_status
                                  CHECK (
                                      status IN (
                                                 'PENDING_REVIEW',
                                                 'REJECTED',
                                                 'OPEN',
                                                 'ACCEPTED',
                                                 'COMPLETION_PENDING',
                                                 'COMPLETED',
                                                 'CANCELLED'
                                          )
                                      ),

                              CONSTRAINT ck_rescue_order_location
                                  CHECK (
                                      (
                                          location_text IS NOT NULL
                                              AND TRIM(location_text) <> ''
                                          )
                                          OR (
                                          latitude IS NOT NULL
                                              AND longitude IS NOT NULL
                                              AND latitude BETWEEN -90 AND 90
                                              AND longitude BETWEEN -180 AND 180
                                          )
                                      ),

                              CONSTRAINT fk_rescue_order_reporter
                                  FOREIGN KEY (reporter_id)
                                      REFERENCES sys_user (id),

                              CONSTRAINT fk_rescue_order_reviewer
                                  FOREIGN KEY (reviewer_id)
                                      REFERENCES sys_user (id),

                              CONSTRAINT fk_rescue_order_volunteer
                                  FOREIGN KEY (volunteer_id)
                                      REFERENCES sys_user (id),

                              CONSTRAINT fk_rescue_order_confirmer
                                  FOREIGN KEY (confirmer_id)
                                      REFERENCES sys_user (id),

                              INDEX idx_rescue_order_status_created
                                  (status, created_at),

                              INDEX idx_rescue_order_reporter_created
                                  (reporter_id, created_at),

                              INDEX idx_rescue_order_volunteer_status
                                  (volunteer_id, status)
) ENGINE = InnoDB
  DEFAULT CHARACTER SET = utf8mb4
  COLLATE = utf8mb4_unicode_ci
  COMMENT = '海洋环境与动物救助工单表';


CREATE TABLE rescue_order_image (
                                    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '图片主键',

                                    rescue_order_id BIGINT NOT NULL COMMENT '所属工单编号',

                                    image_type VARCHAR(20) NOT NULL COMMENT '图片所属阶段',

                                    image_url VARCHAR(500) NOT NULL COMMENT '图片访问地址',

                                    uploaded_by BIGINT NOT NULL COMMENT '图片上传用户编号',

                                    sort_order INT NOT NULL DEFAULT 0 COMMENT '图片显示顺序',

                                    created_at DATETIME NOT NULL
                                                            DEFAULT CURRENT_TIMESTAMP
                                        COMMENT '创建时间',

                                    CONSTRAINT ck_rescue_order_image_type
                                        CHECK (
                                            image_type IN (
                                                           'REPORT',
                                                           'COMPLETION'
                                                )
                                            ),

                                    CONSTRAINT fk_rescue_order_image_order
                                        FOREIGN KEY (rescue_order_id)
                                            REFERENCES rescue_order (id),

                                    CONSTRAINT fk_rescue_order_image_uploader
                                        FOREIGN KEY (uploaded_by)
                                            REFERENCES sys_user (id),

                                    INDEX idx_rescue_order_image_order_type
                                        (rescue_order_id, image_type, sort_order)
) ENGINE = InnoDB
  DEFAULT CHARACTER SET = utf8mb4
  COLLATE = utf8mb4_unicode_ci
  COMMENT = '救助工单图片表';