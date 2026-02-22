-- ============================================================
-- 课程目标达成情况管理系统 - 数据库初始化脚本
-- 数据库: course_target
-- ============================================================

USE course_target;

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ============================================================
-- 一、RBAC 权限管理表（5张）
-- ============================================================

-- 1. 用户表
DROP TABLE IF EXISTS sys_user;
CREATE TABLE sys_user (
  user_id     BIGINT       NOT NULL AUTO_INCREMENT  COMMENT '主键ID',
  username    VARCHAR(50)  NOT NULL                 COMMENT '登录用户名',
  password    VARCHAR(200) NOT NULL                 COMMENT '登录密码(BCrypt加密)',
  display_name VARCHAR(50) DEFAULT ''               COMMENT '显示姓名',
  status      CHAR(1)      DEFAULT '1'              COMMENT '状态(0停用 1正常)',
  create_time DATETIME     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  update_time DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (user_id),
  UNIQUE KEY uk_username (username)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- 2. 角色表
DROP TABLE IF EXISTS sys_role;
CREATE TABLE sys_role (
  role_id     BIGINT       NOT NULL AUTO_INCREMENT  COMMENT '主键ID',
  role_key    VARCHAR(50)  NOT NULL                 COMMENT '角色标识(director/teacher)',
  role_name   VARCHAR(50)  NOT NULL                 COMMENT '角色名称(系主任/任课教师)',
  sort_order  INT          DEFAULT 0                COMMENT '显示排序',
  status      CHAR(1)      DEFAULT '1'              COMMENT '状态(0停用 1正常)',
  create_time DATETIME     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (role_id),
  UNIQUE KEY uk_role_key (role_key)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色表';

-- 3. 用户角色关联表
DROP TABLE IF EXISTS sys_user_role;
CREATE TABLE sys_user_role (
  user_id BIGINT NOT NULL COMMENT '用户ID',
  role_id BIGINT NOT NULL COMMENT '角色ID',
  PRIMARY KEY (user_id, role_id),
  KEY idx_role_id (role_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户角色关联表';

-- 4. 菜单权限表
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

-- 5. 角色菜单关联表
DROP TABLE IF EXISTS sys_role_menu;
CREATE TABLE sys_role_menu (
  role_id BIGINT NOT NULL COMMENT '角色ID',
  menu_id BIGINT NOT NULL COMMENT '菜单ID',
  PRIMARY KEY (role_id, menu_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色菜单关联表';

-- ============================================================
-- 二、业务数据表（5张 + 1张分配表）
-- ============================================================

-- 6. 教师信息表
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

-- 7. 课程信息表
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

-- 8. 课程目标表
DROP TABLE IF EXISTS course_objective;
CREATE TABLE course_objective (
  id           BIGINT       NOT NULL AUTO_INCREMENT  COMMENT '主键ID',
  course_id    BIGINT       NOT NULL                 COMMENT '所属课程(FK→course)',
  tag          VARCHAR(20)  NOT NULL                 COMMENT '目标编号(目标一/目标二)',
  description  TEXT                                  COMMENT '目标描述',
  requirements JSON                                  COMMENT '毕业要求(JSON存放多条)',
  indicator    VARCHAR(50)  DEFAULT ''               COMMENT '支撑指标点(1.2)',
  weight       VARCHAR(20)  DEFAULT ''               COMMENT '权重(40%)',
  create_time  DATETIME     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  update_time  DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (id),
  KEY idx_course_id (course_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='课程目标表';

-- 9. 教学任务表
DROP TABLE IF EXISTS course_task;
CREATE TABLE course_task (
  id               BIGINT       NOT NULL AUTO_INCREMENT  COMMENT '主键ID',
  course_id        BIGINT       NOT NULL                 COMMENT '所属课程(FK→course)',
  semester         VARCHAR(50)  NOT NULL                 COMMENT '执行学期',
  status           VARCHAR(20)  DEFAULT '未提交'          COMMENT '提交状态(未提交/已提交)',
  achievement_rate DECIMAL(5,2) DEFAULT NULL             COMMENT '综合达成率(%)',
  student_count    INT          DEFAULT 0                COMMENT '总学生人数',
  create_time      DATETIME     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  update_time      DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (id),
  KEY idx_course_id (course_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='教学任务表';

-- 10. 任务分配表
DROP TABLE IF EXISTS task_assignment;
CREATE TABLE task_assignment (
  id             BIGINT       NOT NULL AUTO_INCREMENT  COMMENT '主键ID',
  task_id        BIGINT       NOT NULL                 COMMENT '所属任务(FK→course_task)',
  teacher_id     BIGINT       NOT NULL                 COMMENT '授课教师(FK→teacher)',
  grade          VARCHAR(50)  DEFAULT ''               COMMENT '专业年级(2023级)',
  classes        VARCHAR(200) DEFAULT ''               COMMENT '分配班级(01班,02班)',
  student_count  INT          DEFAULT 0                COMMENT '学生人数',
  assign_status  VARCHAR(20)  DEFAULT '未分配'          COMMENT '分配状态(已分配/未分配)',
  create_time    DATETIME     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (id),
  KEY idx_task_id (task_id),
  KEY idx_teacher_id (teacher_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='任务分配表';

-- 11. 达成度上传记录表
DROP TABLE IF EXISTS upload_record;
CREATE TABLE upload_record (
  id             BIGINT       NOT NULL AUTO_INCREMENT  COMMENT '主键ID',
  course_id      BIGINT       NOT NULL                 COMMENT '所属课程(FK→course)',
  teacher_id     BIGINT       NOT NULL                 COMMENT '提交教师(FK→teacher)',
  semester       VARCHAR(50)  NOT NULL                 COMMENT '提交学期',
  objective1     DECIMAL(5,2) DEFAULT NULL             COMMENT '目标一达成率(%)',
  objective2     DECIMAL(5,2) DEFAULT NULL             COMMENT '目标二达成率(%)',
  objective3     DECIMAL(5,2) DEFAULT NULL             COMMENT '目标三达成率(%)',
  status         VARCHAR(20)  DEFAULT '待审核'          COMMENT '审核状态(待审核/已通过/已驳回)',
  audit_comment  VARCHAR(500) DEFAULT ''               COMMENT '审核意见/驳回原因',
  file_path      VARCHAR(500) DEFAULT ''               COMMENT '上传Excel文件存放路径',
  upload_time    DATETIME     DEFAULT CURRENT_TIMESTAMP COMMENT '上传时间',
  update_time    DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (id),
  KEY idx_course_id (course_id),
  KEY idx_teacher_id (teacher_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='达成度上传记录表';

SET FOREIGN_KEY_CHECKS = 1;

-- ============================================================
-- 三、初始数据
-- ============================================================

-- 角色
INSERT INTO sys_role (role_id, role_key, role_name, sort_order) VALUES
(1, 'director', '系主任', 1),
(2, 'teacher',  '任课教师', 2);

-- 默认用户 (密码均为 BCrypt 加密后的 "123456")
INSERT INTO sys_user (user_id, username, password, display_name) VALUES
(1, 'director', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6CQVyFzER.o3Rqo6wlf5cYEBK', '系主任'),
(2, 'teacher',  '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6CQVyFzER.o3Rqo6wlf5cYEBK', '张明');

-- 用户角色关联
INSERT INTO sys_user_role (user_id, role_id) VALUES
(1, 1),
(2, 2);

-- 菜单 (系统所有页面)
INSERT INTO sys_menu (menu_id, menu_name, parent_id, path, icon, perms, menu_type, sort_order) VALUES
(1,  '首页',           0, '/dashboard',            'House',        '',                    'C', 1),
(2,  '课程目标管理',   0, '/course-objectives',    'Aim',          'course:objective:list','C', 2),
(3,  '课程目标维护',   0, '/objective-maintenance','Setting',      'course:objective:edit','C', 3),
(4,  '课程任务管理',   0, '/course-task-management','Tickets',     'course:task:manage',  'C', 4),
(5,  '课程任务查看',   0, '/course-tasks',         'List',         'course:task:view',    'C', 5),
(6,  '教师信息管理',   0, '/teacher-management',   'User',         'teacher:manage',      'C', 6),
(7,  '达成数据上传',   0, '/upload',               'Upload',       'upload:data',         'C', 7),
(8,  '达成数据审核',   0, '/data-audit',           'DocumentChecked','upload:audit',      'C', 8),
(9,  '达成情况查询',   0, '/query',                'Search',       'achievement:query',   'C', 9),
(10, '数据可视化',     0, '/visualization',        'DataAnalysis', 'data:visual',         'C', 10);

-- 系主任拥有全部菜单
INSERT INTO sys_role_menu (role_id, menu_id) VALUES
(1,1),(1,2),(1,3),(1,4),(1,5),(1,6),(1,7),(1,8),(1,9),(1,10);

-- 教师拥有部分菜单
INSERT INTO sys_role_menu (role_id, menu_id) VALUES
(2,1),(2,5),(2,7),(2,9);

-- 教师档案
INSERT INTO teacher (id, user_id, name, gender, phone, dept, title, status) VALUES
(1, 2, '张明', '男', '13800138001', '数学系', '副教授', '1'),
(2, NULL, '李华', '女', '13800138002', '数学系', '讲师', '1'),
(3, NULL, '王芳', '女', '13800138003', '数学系', '教授', '1'),
(4, NULL, '刘强', '男', '13800138004', '计算机系', '副教授', '1'),
(5, NULL, '陈梅', '女', '13800138005', '计算机系', '讲师', '1');

-- 课程
INSERT INTO course (id, code, name, dept, credits, hours, status) VALUES
(1, 'MATH2101', '高等数学A', '数学系', 4.0, 64, '1'),
(2, 'MATH2102', '线性代数',  '数学系', 3.0, 48, '1'),
(3, 'MATH3201', '概率论与数理统计', '数学系', 3.0, 48, '0'),
(4, 'CS2101',   '数据结构',  '计算机系', 4.0, 64, '1'),
(5, 'CS3101',   '操作系统',  '计算机系', 3.0, 48, '1');

-- 课程目标
INSERT INTO course_objective (id, course_id, tag, description, requirements, indicator, weight) VALUES
(1, 1, '目标一', '使学生掌握极限、导数、积分等微积分基础知识，能解决工程中的数学问题', '["工程知识"]', '指标点1.2', '40%'),
(2, 1, '目标二', '培养学生数学建模能力，用数学工具分析和解决实际问题', '["问题分析"]', '指标点2.1', '35%'),
(3, 1, '目标三', '通过课堂讨论培养学生团队协作与表达能力', '["个人和团队"]', '指标点9.1', '25%'),
(4, 2, '目标一', '掌握矩阵运算、行列式、线性方程组的基础理论', '["工程知识"]', '指标点1.1', '50%'),
(5, 2, '目标二', '能将线性代数理论应用于电路分析等工程问题', '["问题分析"]', '指标点2.2', '50%');

-- 教学任务
INSERT INTO course_task (id, course_id, semester, status, achievement_rate, student_count) VALUES
(1, 1, '2023-2024第一学期', '已提交', 85.20, 185),
(2, 2, '2023-2024第一学期', '已提交', 78.60, 110),
(3, 3, '2023-2024第一学期', '未提交', NULL, 0);

-- 任务分配
INSERT INTO task_assignment (id, task_id, teacher_id, grade, classes, student_count, assign_status) VALUES
(1, 1, 1, '2023级', '01班,02班', 110, '已分配'),
(2, 1, 2, '2023级', '03班', 75, '已分配'),
(3, 2, 3, '2023级', '01班', 110, '已分配');

-- 上传记录
INSERT INTO upload_record (id, course_id, teacher_id, semester, objective1, objective2, objective3, status) VALUES
(1, 1, 1, '2023-2024第一学期', 85.20, 82.10, 88.50, '已通过'),
(2, 3, 3, '2023-2024第二学期', 78.60, 75.30, 81.20, '待审核'),
(3, 2, 2, '2023-2024第一学期', 72.10, 68.50, NULL, '已驳回'),
(4, 4, 4, '2023-2024第一学期', 88.30, 90.10, 86.70, '已通过');
