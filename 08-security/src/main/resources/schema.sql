-- 09-security 实训任务提交与批改：先删后建，每次启动重跑，表结构与本机库导出的《任务批改数据表.sql》一致。
-- 与 05-mybatis 共用 backend_learning 库，表名统一 sec_ 前缀。

-- 先删有外键指向的表，再删被指向的表
DROP TABLE IF EXISTS sec_task;
DROP TABLE IF EXISTS sec_user_role;
DROP TABLE IF EXISTS sec_user;
DROP TABLE IF EXISTS sec_role;

-- 角色表：只存角色本身，权限点留在 RolePermissions 映射里，跟着接口一起评审
CREATE TABLE sec_role (
    id   BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
    code VARCHAR(20)     NOT NULL COMMENT '角色编码，STUDENT / TEACHER / ADMIN',
    name VARCHAR(50)     NOT NULL COMMENT '角色名称，给页面显示',
    PRIMARY KEY (id),
    UNIQUE KEY uk_role_code (code)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '角色';

-- 用户表：password 存 BCrypt 密文（定长 60，列宽留 100 以便换更长的算法）
CREATE TABLE sec_user (
    id         BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
    username   VARCHAR(50)     NOT NULL COMMENT '登录名',
    password   VARCHAR(100)    NOT NULL COMMENT 'BCrypt 密文，不可逆',
    nickname   VARCHAR(50)     NOT NULL COMMENT '姓名，页面上显示',
    enabled    TINYINT         NOT NULL DEFAULT 1 COMMENT '1 启用 / 0 停用，停用账号登录会被拒',
    created_at DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_user_username (username)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '账号';

-- 用户角色关联表：一个用户可以有多个角色，主键就是这两列的组合
CREATE TABLE sec_user_role (
    user_id BIGINT UNSIGNED NOT NULL COMMENT '用户 id',
    role_id BIGINT UNSIGNED NOT NULL COMMENT '角色 id',
    PRIMARY KEY (user_id, role_id),
    KEY idx_user_role_role (role_id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '用户与角色的关联';

-- 任务表：归属用 owner_id 指向 sec_user，不冗余存用户名
CREATE TABLE sec_task (
    id           BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
    title        VARCHAR(100)    NOT NULL COMMENT '任务标题',
    content      VARCHAR(2000)   NOT NULL COMMENT '完成情况说明',
    owner_id     BIGINT UNSIGNED NOT NULL COMMENT '提交人，指向 sec_user.id',
    status       VARCHAR(20)     NOT NULL DEFAULT 'SUBMITTED' COMMENT 'SUBMITTED 待批改 / REVIEWED 已批改',
    score        TINYINT UNSIGNED         DEFAULT NULL COMMENT '分数 0 到 100，未批改时为空',
    comment      VARCHAR(500)             DEFAULT NULL COMMENT '教师评语',
    submitted_at DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '提交时间',
    reviewed_at  DATETIME                 DEFAULT NULL COMMENT '批改时间',
    PRIMARY KEY (id),
    KEY idx_task_owner (owner_id),
    KEY idx_task_status (status)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '实训任务提交记录';


-- 课程案例独立表，保留原 sec_* 任务系统结构。
CREATE TABLE IF NOT EXISTS lesson_user (
 id BIGINT PRIMARY KEY AUTO_INCREMENT,
 username VARCHAR(64) NOT NULL UNIQUE,
 phone VARCHAR(20) UNIQUE,
 password VARCHAR(128) NOT NULL,
 nickname VARCHAR(64),
 enabled BOOLEAN NOT NULL DEFAULT TRUE,
 locked BOOLEAN NOT NULL DEFAULT FALSE
);
CREATE TABLE IF NOT EXISTS lesson_role (
 id BIGINT PRIMARY KEY AUTO_INCREMENT,
 user_id BIGINT NOT NULL,
 role_code VARCHAR(64) NOT NULL,
 grant_type VARCHAR(8) NOT NULL DEFAULT 'ROLE',
 UNIQUE (user_id, role_code)
);
