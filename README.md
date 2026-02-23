# 课程目标达成度系统 (Course Target Achievement System)

## 项目简介
本系统致力于为各类高等院校与教育机构提供一站式的“课程目标达成度”计算与数据可视化服务，核心业务目标是通过客观数据全面反映课程的整体教学质量。
通过本系统，教师可以快捷上传和维护学生平时考核和期末考试等各类原始成绩数据，系统将基于设定的权重和计算公式，全自动计算得出每项具体的**课程目标达成度**并动态生成可视化报表（如：蜘蛛网雷达图、全系横向对比柱状图、多学期纵向折线图）。

**核心功能模块：**
- **角色权限管理**：区分教师、系主任（系统管理员）等多级角色，分别控制操作与查看范围。
- **课程及目标维护**：构建课程信息库，对各门课程的毕业要求、考核指标点、支撑权重进行结构化维护。
- **成绩数据采集**：支持通过 Excel 模板一键导入各类学生成绩单，自动识别容错并校验。
- **动态成绩看板**：利用 ECharts 技术对全系宏观数据统计、课程表现短板、个人教学历史轨迹做可视化追踪分析。

---

## 技术栈声明

本系统采用主流的前后端分离架构，兼顾高并发安全性与优质交互体验。

### 前端核心技术
- **核心框架**：Vue 3 (基于 Composition API + `<script setup>` 语法糖)
- **工程化构建构建**：Vite 5.x
- **UI 组件库**：Element Plus
- **状态管理**：Pinia
- **图表可视化引擎**：Apache ECharts 5.x
- **网络请求**：Axios (支持请求拦截自动注入 JWT Token 和 全局 401 登出处理)

### 后端核心技术
- **核心框架**：Spring Boot 3.x / Java 17+
- **持久层框架**：MyBatis-Plus
- **数据库连接池**：Druid
- **关系型数据库**：MySQL 8.0
- **缓存与日志队列**：Redis (承担 Token 生命周期托管、高频动态活动日志记录等非持久化操作)
- **安全认证模块**：Spring Security 6.x + JWT (Json Web Token)
- **办公文档解析**：阿里 EasyExcel (实现高速/低内存的 Excel 成绩单流式解析导入)
- **接口调试文档**：Knife4j (Swagger)
- **其他组件**：Lombok 等

---

## 启动流程

> **环境依赖准备**：在部署前，请确保本地环境中已成功安装 **Java 17+**, **Node.js 18+**, **MySQL 8.0+**, **Redis 6.x+**, 以及 **Maven**，并保持其正在运行。

### 第 1 步：初始化数据库
1. 登录本地 MySQL，创建一个新的数据库实例（例如命名为 `course_target` ；字符集推荐 `utf8mb4`）。
2. 使用 Navicat 或命令行等数据库管理工具，选中此空库。
3. 执行本项目后端的存放 SQL 初始化脚本文件：`course-target-server/sql/init.sql`。
4. 确认所有表（例如：`sys_user`, `teacher`, `course`, `course_objective`, `upload_record` 等）均已成功创建并被注入基础测试数据。

### 第 2 步：启动 Spring Boot 后端项目
1. 建议使用 IntelliJ IDEA 将工作目录导入至项目根节点的 `course-target-server` 文件夹。
2. 找到 `src/main/resources/application.yml` 配置文件。
3. 修改配置属性中的 `spring.datasource` 信息为你刚创建的本地 MySQL 地址、账号密码；同时确认 `spring.data.redis` 节点中的 Redis 配置参数准确无误。
4. 运行 Maven `clean` 及 `install` 以下载所有 Java 依赖包。
5. 运行启动类 `com.fmk.cource.ServerApplication` 开启后台运行服务（默认暴露在 `8088` 端口）。

### 第 3 步：启动 Vue 前端项目
1. 另开一个命令行终端，进入到前端所在文件夹目录：
   ```bash
   cd course-target-ui
   ```
2. 安装 NPM 全家桶包（初次或删掉 `node_modules` 的情况下执行）：
   ```bash
   npm install
   ```
3. 激活前端调试服务器并在浏览器中实时展示：
   ```bash
   npm run dev
   ```
4. 控制台将会打印一串如 `http://localhost:5173/` 的网络地址。使用浏览器打开，登入系统（测试账号可参照初始化数据表中的 `sys_user` 表记录：如以张明老师的身份使用 `zhangming` 登录）。
 