CREATE TABLE sys_role (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键',
    role_code VARCHAR(50) NOT NULL COMMENT '角色编码',
    role_name VARCHAR(50) NOT NULL COMMENT '角色名称',
    description VARCHAR(255) NULL COMMENT '角色说明',
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE' COMMENT '角色状态',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
        ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    CONSTRAINT uk_sys_role_code UNIQUE (role_code)
) ENGINE = InnoDB
  DEFAULT CHARACTER SET = utf8mb4
  COLLATE = utf8mb4_unicode_ci
  COMMENT = '系统角色表';

CREATE TABLE sys_user_role (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键',
    user_id BIGINT NOT NULL COMMENT '用户编号',
    role_id BIGINT NOT NULL COMMENT '角色编号',
    assigned_by BIGINT NULL COMMENT '角色授予人',
    assigned_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '授予时间',
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE' COMMENT '授权状态',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
        ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    CONSTRAINT uk_sys_user_role UNIQUE (user_id, role_id),
    CONSTRAINT fk_user_role_user
        FOREIGN KEY (user_id) REFERENCES sys_user (id),
    CONSTRAINT fk_user_role_role
        FOREIGN KEY (role_id) REFERENCES sys_role (id),
    CONSTRAINT fk_user_role_assigner
        FOREIGN KEY (assigned_by) REFERENCES sys_user (id)
) ENGINE = InnoDB
  DEFAULT CHARACTER SET = utf8mb4
  COLLATE = utf8mb4_unicode_ci
  COMMENT = '用户角色关联表';

INSERT INTO sys_role (role_code, role_name, description)
VALUES
    ('USER', '普通用户', '使用系统基础功能'),
    ('VOLUNTEER', '志愿者', '接受并处理救助工单'),
    ('ADMIN', '管理员', '管理系统数据和审核业务申请');
