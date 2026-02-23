/* 课程目标达成情况管理系统 - 数据库初始化脚本 */
/* 数据库: course_target */

USE course_target;

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

/* 一、RBAC 权限管理表 */

/* 1. 用户表 */
DROP TABLE IF EXISTS sys_user;
CREATE TABLE sys_user (
  user_id      BIGINT       NOT NULL AUTO_INCREMENT  COMMENT '主键ID',
  username     VARCHAR(50)  NOT NULL                 COMMENT '登录用户名',
  password     VARCHAR(200) NOT NULL                 COMMENT '登录密码(BCrypt加密)',
  display_name VARCHAR(50)  DEFAULT ''               COMMENT '显示姓名',
  status       CHAR(1)      DEFAULT '1'              COMMENT '状态(0停用 1正常)',
  create_time  DATETIME     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  update_time  DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (user_id),
  UNIQUE KEY uk_username (username)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

/* 2. 角色表 */
DROP TABLE IF EXISTS sys_role;
CREATE TABLE sys_role (
  role_id     BIGINT       NOT NULL AUTO_INCREMENT  COMMENT '主键ID',
  role_key    VARCHAR(50)  NOT NULL                 COMMENT '角色标识(1为director,2为teacher)',
  role_name   VARCHAR(50)  NOT NULL                 COMMENT '角色名称(系主任/任课教师)',
  sort_order  INT          DEFAULT 0                COMMENT '显示排序',
  status      CHAR(1)      DEFAULT '1'              COMMENT '状态(0停用 1正常)',
  create_time DATETIME     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (role_id),
  UNIQUE KEY uk_role_key (role_key)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色表';

/* 3. 用户角色关联表 */
DROP TABLE IF EXISTS sys_user_role;
CREATE TABLE sys_user_role (
  user_id BIGINT NOT NULL COMMENT '用户ID',
  role_id BIGINT NOT NULL COMMENT '角色ID',
  PRIMARY KEY (user_id, role_id),
  KEY idx_role_id (role_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户角色关联表';

/* 4. 菜单权限表 */
DROP TABLE IF EXISTS sys_menu;
CREATE TABLE sys_menu (
  menu_id    BIGINT       NOT NULL AUTO_INCREMENT  COMMENT '主键ID',
  menu_name  VARCHAR(50)  NOT NULL                 COMMENT '菜单名称',
  parent_id  BIGINT       DEFAULT 0                COMMENT '父菜单ID(0为顶级)',
  path       VARCHAR(200) DEFAULT ''               COMMENT '路由地址',
  icon       VARCHAR(100) DEFAULT ''               COMMENT '菜单图标',
  perms      VARCHAR(100) DEFAULT ''               COMMENT '权限标识',
  menu_type  CHAR(1)      DEFAULT 'C'              COMMENT '类型(M目录 C菜单 F按钮)',
  sort_order INT          DEFAULT 0                COMMENT '显示排序',
  status     CHAR(1)      DEFAULT '1'              COMMENT '状态(0停用 1正常)',
  PRIMARY KEY (menu_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='菜单权限表';

/* 5. 角色菜单关联表 */
DROP TABLE IF EXISTS sys_role_menu;
CREATE TABLE sys_role_menu (
  role_id BIGINT NOT NULL COMMENT '角色ID',
  menu_id BIGINT NOT NULL COMMENT '菜单ID',
  PRIMARY KEY (role_id, menu_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色菜单关联表';


/* 二、业务数据表 */

/* 6. 教师信息表 */
DROP TABLE IF EXISTS teacher;
CREATE TABLE teacher (
  id          BIGINT       NOT NULL AUTO_INCREMENT  COMMENT '主键ID',
  user_id     BIGINT       DEFAULT NULL             COMMENT '关联账号(FK→sys_user)',
  name        VARCHAR(50)  NOT NULL                 COMMENT '教师姓名',
  gender      VARCHAR(10)  DEFAULT ''               COMMENT '性别',
  phone       VARCHAR(20)  DEFAULT ''               COMMENT '联系电话',
  dept        VARCHAR(100) DEFAULT ''               COMMENT '所属院系',
  title       VARCHAR(50)  DEFAULT ''               COMMENT '职称(教授/副教授/讲师)',
  status      CHAR(1)      DEFAULT '1'              COMMENT '状态(1在职 0离职)',
  create_time DATETIME     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  update_time DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (id),
  KEY idx_user_id (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='教师信息表';

/* 7. 课程信息表 */
DROP TABLE IF EXISTS course;
CREATE TABLE course (
  id          BIGINT       NOT NULL AUTO_INCREMENT  COMMENT '主键ID',
  code        VARCHAR(50)  NOT NULL                 COMMENT '课程编号',
  name        VARCHAR(100) NOT NULL                 COMMENT '课程名称',
  dept        VARCHAR(100) DEFAULT ''               COMMENT '开课院系',
  credits     DECIMAL(3,1) DEFAULT 0                COMMENT '学分',
  hours       INT          DEFAULT 0                COMMENT '学时',
  status      CHAR(1)      DEFAULT '0'              COMMENT '状态(0草稿 1已发布)',
  create_time DATETIME     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  update_time DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (id),
  UNIQUE KEY uk_code (code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='课程信息表';

/* 8. 课程目标表 */
DROP TABLE IF EXISTS course_objective;
CREATE TABLE course_objective (
  id           BIGINT       NOT NULL AUTO_INCREMENT  COMMENT '主键ID',
  course_id    BIGINT       NOT NULL                 COMMENT '所属课程(FK→course)',
  tag          VARCHAR(20)  NOT NULL                 COMMENT '目标编号(课程目标n)',
  description  TEXT                                  COMMENT '目标描述',
  requirements JSON                                  COMMENT '毕业要求(JSON存放多条)',
  indicator    VARCHAR(50)  DEFAULT ''               COMMENT '支撑指标点(1.2)',
  weight       VARCHAR(20)  DEFAULT ''               COMMENT '权重(40%)',
  create_time  DATETIME     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  update_time  DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (id),
  KEY idx_course_id (course_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='课程目标表';

/* 9. 教学任务表 */
DROP TABLE IF EXISTS course_task;
CREATE TABLE course_task (
  id               BIGINT       NOT NULL AUTO_INCREMENT  COMMENT '主键ID',
  course_id        BIGINT       NOT NULL                 COMMENT '所属课程(FK→course)',
  semester         VARCHAR(50)  NOT NULL                 COMMENT '执行学期',
  status           VARCHAR(20)  DEFAULT '未分配'         COMMENT '任务状态(未分配/已分配)',
  achievement_rate DECIMAL(5,2) DEFAULT NULL             COMMENT '综合达成率(%)',
  student_count    INT          DEFAULT 0                COMMENT '总学生人数',
  create_time      DATETIME     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  update_time      DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (id),
  KEY idx_course_id (course_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='教学任务表';

/* 10. 任务分配表 */
DROP TABLE IF EXISTS task_assignment;
CREATE TABLE task_assignment (
  id            BIGINT       NOT NULL AUTO_INCREMENT  COMMENT '主键ID',
  task_id       BIGINT       NOT NULL                 COMMENT '所属任务(FK→course_task)',
  teacher_id    BIGINT       NOT NULL                 COMMENT '授课教师(FK→teacher)',
  grade         VARCHAR(50)  DEFAULT ''               COMMENT '专业年级(2023级)',
  classes       VARCHAR(200) DEFAULT ''               COMMENT '分配班级(01班,02班)',
  student_count INT          DEFAULT 0                COMMENT '学生人数',
  assign_status VARCHAR(20)  DEFAULT '未分配'         COMMENT '分配状态(已分配/未分配)',
  create_time   DATETIME     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (id),
  KEY idx_task_id (task_id),
  KEY idx_teacher_id (teacher_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='任务分配表';

/* 11. 达成度上传记录主表 */
DROP TABLE IF EXISTS upload_record;
CREATE TABLE upload_record (
  id            BIGINT       NOT NULL AUTO_INCREMENT  COMMENT '主键ID',
  course_id     BIGINT       NOT NULL                 COMMENT '所属课程(FK→course)',
  teacher_id    BIGINT       NOT NULL                 COMMENT '提交教师(FK→teacher)',
  semester      VARCHAR(50)  NOT NULL                 COMMENT '提交学期',
  status        VARCHAR(20)  DEFAULT '待审核'         COMMENT '审核状态(待审核/已通过/已驳回)',
  audit_comment VARCHAR(500) DEFAULT ''               COMMENT '审核意见/驳回原因',
  file_path     VARCHAR(500) DEFAULT ''               COMMENT '上传Excel文件存放路径',
  upload_time   DATETIME     DEFAULT CURRENT_TIMESTAMP COMMENT '上传时间',
  update_time   DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (id),
  KEY idx_course_id (course_id),
  KEY idx_teacher_id (teacher_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='达成度上传记录表';

/* 12. 达成度上传记录明细表 */
DROP TABLE IF EXISTS upload_record_detail;
CREATE TABLE upload_record_detail (
  id               BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  record_id        BIGINT       NOT NULL                COMMENT '关联的上传记录(FK→upload_record)',
  objective_id     BIGINT       NOT NULL                COMMENT '课程目标ID(对应course_objective表的id)',
  achievement_rate DECIMAL(5,2) DEFAULT NULL            COMMENT '该目标的达成率(%)',
  create_time      DATETIME     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (id),
  KEY idx_record_id (record_id),
  KEY idx_objective_id (objective_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='达成度上传明细表';

SET FOREIGN_KEY_CHECKS = 1;

