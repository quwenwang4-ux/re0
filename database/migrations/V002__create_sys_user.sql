CREATE TABLE sys_user (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键',
    username VARCHAR(50) NOT NULL COMMENT '登录账号',
    password_hash VARCHAR(255) NOT NULL COMMENT '密码散列值',
    nickname VARCHAR(100) NULL COMMENT '用户昵称',
    email VARCHAR(255) NULL COMMENT '邮箱',
    phone VARCHAR(30) NULL COMMENT '手机号',
    avatar_url VARCHAR(500) NULL COMMENT '头像地址',
    status VARCHAR(30) NOT NULL DEFAULT 'ACTIVE' COMMENT '账号状态',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
        ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除标记',
    CONSTRAINT uk_sys_user_username UNIQUE (username),
    CONSTRAINT uk_sys_user_email UNIQUE (email),
    CONSTRAINT uk_sys_user_phone UNIQUE (phone),
    INDEX idx_sys_user_status (status),
    INDEX idx_sys_user_deleted (deleted)
) ENGINE = InnoDB
  DEFAULT CHARACTER SET = utf8mb4
  COLLATE = utf8mb4_unicode_ci
  COMMENT = '系统用户表';
