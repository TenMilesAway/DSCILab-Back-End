# DSCILab-Back-End 项目架构分析文档

## 1. 项目架构概述

### 1.1 整体架构类型
这个项目采用的是**DDD（领域驱动设计）+ 分层架构**，而不是传统的MVC架构。

### 1.2 与MVC架构的区别

| 方面 | 传统MVC架构 | 本项目DDD架构 |
|------|-------------|---------------|
| **分层方式** | Model-View-Controller 三层 | 按业务领域和技术关注点分层 |
| **核心思想** | 技术分层，关注数据流转 | 业务领域驱动，关注业务逻辑 |
| **代码组织** | 按技术功能分包(controller/service/dao) | 按业务领域分包(user/role/menu等) |
| **业务逻辑** | 主要在Service层 | 分布在Domain Model和Application Service |
| **复杂度处理** | 随业务增长变得复杂 | 通过领域模型封装复杂业务逻辑 |
| **可维护性** | 中等，技术耦合较强 | 高，业务内聚，技术解耦 |

### 1.3 项目分层架构

```
┌─────────────────────────────────────────┐
│           agileboot-admin               │  ← 管理端接口层
│           agileboot-api                 │  ← API接口层  
├─────────────────────────────────────────┤
│           agileboot-domain              │  ← 领域层(核心业务)
├─────────────────────────────────────────┤
│        agileboot-infrastructure         │  ← 基础设施层
├─────────────────────────────────────────┤
│           agileboot-common              │  ← 通用工具层
└─────────────────────────────────────────┘
```

## 2. 各模块详细分析

### 2.1 agileboot-admin (管理端接口层)
**作用**: 提供后台管理系统的REST API接口

**主要文件作用**:
- `LoginController.java`: 处理登录、登出、验证码、路由获取等
- `SysUserController.java`: 用户的增删改查、导入导出等
- `SysRoleController.java`: 角色管理、权限分配等
- `SysMenuController.java`: 菜单权限管理
- `SysDeptController.java`: 部门组织架构管理
- `SysPostController.java`: 职位岗位管理
- `SysNoticeController.java`: 系统公告管理

### 2.2 agileboot-api (API接口层)
**作用**: 预留的移动端或第三方API接口

### 2.3 agileboot-domain (领域层 - 核心)
**作用**: 包含核心业务逻辑和领域模型

**核心概念**:
- `ApplicationService`: 应用服务，协调领域对象完成业务用例
- `Entity`: 实体类，对应数据库表
- `Model`: 领域模型，包含业务逻辑
- `Command`: 命令对象，封装操作参数
- `Query`: 查询对象，封装查询条件
- `DTO`: 数据传输对象，用于层间数据传递

### 2.4 agileboot-infrastructure (基础设施层)
**作用**: 提供技术基础设施支持

### 2.5 agileboot-common (通用工具层)
**作用**: 提供通用工具类和基础组件

## 3. 数据来源和格式

### 3.1 数据库配置
- **数据库类型**: MySQL
- **连接配置**: `application-dev.yml`中配置
- **默认数据库**: `web` (可在配置文件中修改)
- **连接信息**: 
  - URL: `jdbc:mysql://localhost:3307/web`
  - 用户名: `root`
  - 密码: `12345`

### 3.2 数据访问层技术栈
- **ORM框架**: MyBatis Plus
- **连接池**: Druid
- **数据库驱动**: MySQL Connector
- **分页插件**: MyBatis Plus内置分页

### 3.3 主要数据表结构
基于代码分析，主要包含以下核心表：
- `sys_user`: 用户信息表
- `sys_role`: 角色信息表  
- `sys_menu`: 菜单权限表
- `sys_dept`: 部门信息表
- `sys_post`: 职位信息表
- `sys_notice`: 系统公告表
- `sys_role_menu`: 角色菜单关联表

### 3.4 数据获取方式
1. **Repository层**: 通过MyBatis Mapper接口访问数据库
2. **Service层**: 封装数据访问逻辑
3. **缓存层**: 使用Redis和Guava Cache提升性能
4. **数据源**: 支持主从数据库配置

## 4. 前端接口规范

### 4.1 接口基础信息
- **基础路径**: `/` (根路径)
- **认证方式**: JWT Token
- **数据格式**: JSON
- **响应格式**: 统一的ResponseDTO包装

### 4.2 统一响应格式
```json
{
    "code": 200,           // 状态码
    "message": "操作成功",  // 消息
    "data": {},           // 数据
    "timestamp": 1234567890 // 时间戳
}
```

### 4.3 主要接口分类

#### 4.3.1 认证相关接口
- `GET /`: 系统首页信息
- `GET /getConfig`: 获取系统配置
- `GET /captchaImage`: 获取验证码
- `POST /login`: 用户登录
- `POST /logout`: 用户登出
- `GET /getRouters`: 获取用户菜单路由

#### 4.3.2 用户管理接口
- `GET /system/users`: 用户列表查询
- `GET /system/users/{userId}`: 用户详情
- `POST /system/users`: 新增用户
- `PUT /system/users`: 更新用户
- `DELETE /system/users/{userIds}`: 删除用户
- `PUT /system/users/changeStatus`: 修改用户状态
- `PUT /system/users/resetPassword`: 重置密码

#### 4.3.3 角色管理接口  
- `GET /system/role/list`: 角色列表
- `GET /system/role/{roleId}`: 角色详情
- `POST /system/role`: 新增角色
- `PUT /system/role`: 更新角色
- `DELETE /system/role/{roleIds}`: 删除角色
- `PUT /system/role/dataScope`: 数据权限设置

#### 4.3.4 菜单管理接口
- `GET /system/menus`: 菜单列表
- `GET /system/menus/{menuId}`: 菜单详情  
- `POST /system/menus`: 新增菜单
- `PUT /system/menus`: 更新菜单
- `DELETE /system/menus/{menuId}`: 删除菜单
- `GET /system/menus/dropdown`: 菜单下拉树

#### 4.3.5 部门管理接口
- `GET /system/depts`: 部门列表
- `GET /system/depts/{deptId}`: 部门详情
- `POST /system/depts`: 新增部门
- `PUT /system/depts`: 更新部门  
- `DELETE /system/depts/{deptId}`: 删除部门
- `GET /system/depts/dropdown`: 部门下拉树

#### 4.3.6 职位管理接口
- `GET /system/post/list`: 职位列表
- `GET /system/post/{postId}`: 职位详情
- `POST /system/post`: 新增职位
- `PUT /system/post`: 更新职位
- `DELETE /system/post/{postIds}`: 删除职位

#### 4.3.7 公告管理接口
- `GET /system/notices`: 公告列表
- `GET /system/notices/{noticeId}`: 公告详情
- `POST /system/notices`: 新增公告
- `PUT /system/notices`: 更新公告
- `DELETE /system/notices/{noticeIds}`: 删除公告

### 4.4 接口特性
1. **权限控制**: 使用`@PreAuthorize`注解进行权限验证
2. **数据权限**: 支持部门数据权限控制
3. **操作日志**: 使用`@AccessLog`记录操作日志
4. **防重复提交**: 使用`@Unrepeatable`防止重复操作
5. **限流控制**: 使用`@RateLimit`进行接口限流
6. **参数验证**: 使用`@Validated`进行参数校验
7. **API文档**: 集成Swagger/OpenAPI文档

### 4.5 分页查询格式
请求参数:
```json
{
    "pageNum": 1,      // 页码
    "pageSize": 10,    // 每页大小
    "orderBy": "createTime desc", // 排序
    // 其他查询条件...
}
```

响应格式:
```json
{
    "code": 200,
    "data": {
        "records": [],     // 数据列表
        "total": 100,      // 总记录数
        "pageNum": 1,      // 当前页
        "pageSize": 10     // 每页大小
    }
}
```

## 5. 技术栈详解

### 5.1 核心技术栈
- **框架**: Spring Boot 2.x
- **安全**: Spring Security + JWT
- **ORM**: MyBatis Plus
- **数据库**: MySQL 8.0
- **缓存**: Redis + Guava Cache
- **连接池**: Druid
- **文档**: Swagger/OpenAPI 3
- **工具库**: Hutool, Apache Commons
- **构建工具**: Maven

## 6. 开发指南

### 6.1 环境搭建
1. **JDK**: 要求JDK 8或以上版本
2. **数据库**: MySQL 8.0，创建数据库`web`
3. **Redis**: 用于缓存和会话管理
4. **IDE**: 推荐IntelliJ IDEA或Eclipse

### 6.2 启动步骤
1. 导入项目到IDE
2. 修改`application-dev.yml`中的数据库配置
3. 执行SQL脚本初始化数据库
4. 启动Redis服务
5. 运行`AgileBooApiApplication`启动应用

### 6.3 代码规范

#### 6.3.1 命名规范
- **类名**: 使用PascalCase，如`UserApplicationService`
- **方法名**: 使用camelCase，如`getUserList`
- **常量**: 使用UPPER_SNAKE_CASE，如`MAX_RETRY_COUNT`
- **包名**: 使用小写，如`com.agileboot.domain.system.user`

#### 6.3.2 注解使用
- `@RestController`: 标记REST控制器
- `@Service`: 标记服务类
- `@RequiredArgsConstructor`: Lombok生成构造函数
- `@Validated`: 参数校验
- `@PreAuthorize`: 权限控制
- `@AccessLog`: 操作日志记录

## 7. 常见问题

### 7.1 数据库连接问题
- 检查数据库服务是否启动
- 确认连接参数是否正确
- 检查防火墙设置

### 7.2 Redis连接问题
- 确认Redis服务状态
- 检查Redis配置参数
- 验证网络连通性

### 7.3 权限问题
- 检查JWT Token是否有效
- 确认用户角色权限配置
- 验证接口权限注解

---

**总结**: 这是一个基于Spring Boot + MyBatis Plus + DDD架构的企业级后台管理系统，采用前后端分离设计，提供完整的用户权限管理功能，支持多种技术特性如缓存、限流、日志等，具有良好的可扩展性和维护性。通过本文档，开发者可以快速理解项目架构，掌握开发规范，并能够基于现有框架进行二次开发。
