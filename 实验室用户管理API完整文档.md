# 🌐 实验室用户管理系统 - API接口完整文档

## 📋 接口基础信息

### 基础配置
- **Base URL**: `http://localhost:8080`
- **认证方式**: Bearer Token (JWT)
- **Content-Type**: `application/json`
- **字符编码**: UTF-8

### 认证说明
所有需要认证的接口都需要在请求头中添加：
```
Authorization: Bearer {your_jwt_token}
```

### 统一登录说明（重要）
- 统一登录入口：`POST /login`
- 登录协议：沿用系统后台的协议（前端使用 RSA 公钥加密密码后 Base64，后端解密后进行 BCrypt 校验；是否启用验证码以配置为准）
- 用户来源：
  1) 先按 `sys_user` 查询；若不存在，则按 `lab_user` 查询
  2) `lab_user` 登录要求：`is_active=true` 且 `deleted=false`，密码为 BCrypt 存储
- 权限与数据范围：
  - 对于 `lab_user.identity=1`（管理员）：授予 admin 全权限（`*:*:*`），数据范围 `ALL`
  - 对于普通 `lab_user`（identity≠1）：数据范围 `ONLY_SELF`，并默认授予只读权限集：`lab:user:query`、`lab:user:list`
- 旧的 `/lab/auth/login` 已废弃，请统一改用 `/login`


## 🎯 核心接口列表

### 🔍 查询接口
| 接口 | 方法 | 路径 | 权限 | 说明 |
|------|------|------|------|------|
| 获取当前用户信息 | GET | `/lab/users/profile` | 登录用户 | 获取个人详细信息 |
| 分页查询用户列表 | GET | `/lab/users/crud/list` | 管理员 | 支持筛选和分页 |
| 搜索用户 | GET | `/lab/users/crud/search` | 无限制 | 关键词模糊搜索 |
| 检查用户存在 | GET | `/lab/users/{id}/exists` | 登录用户 | 验证用户是否存在 |


### 🌐 开放接口（无需登录）
| 接口 | 方法 | 路径 | 权限 | 说明 |
|------|------|------|------|------|
| 公开用户列表 | GET | `/open/users` | 公开 | 返回公开成员信息（含邮箱）
| 公开用户详情 | GET | `/open/users/{id}` | 公开 | 返回公开成员详情（含邮箱）

### ➕ 创建接口
| 接口 | 方法 | 路径 | 权限 | 说明 |
|------|------|------|------|------|
| 创建新用户 | POST | `/lab/users/crud` | 管理员 | 创建实验室用户 |

### ✏️ 更新接口
| 接口 | 方法 | 路径 | 权限 | 说明 |
|------|------|------|------|------|
| 更新个人信息 | PUT | `/lab/users/crud/profile` | 登录用户 | 修改自己的信息 |
| 修改密码 | PUT | `/lab/users/crud/password` | 登录用户 | 修改登录密码 |
| 管理员更新用户 | PUT | `/lab/users/crud/{id}` | 管理员 | 修改任意用户信息 |

### 🗑️ 删除接口
| 接口 | 方法 | 路径 | 权限 | 说明 |
|------|------|------|------|------|
| 删除用户 | DELETE | `/lab/users/crud/{id}` | 管理员 | 软删除用户 |
| 批量删除 | DELETE | `/lab/users/crud/batch` | 管理员 | 批量删除多个用户 |

## 🔍 用户查询接口

### 1. 获取当前用户信息
```
GET /lab/users/profile
```
**用途**: 获取当前登录用户的详细信息
**权限**: 需要登录
**请求示例**:
```bash
curl -X GET "http://localhost:8080/lab/users/profile" \
  -H "Authorization: Bearer {token}"
```
**响应示例**:
```json
{
  "code": 0,
  "msg": "操作成功",
  "data": {
    "id": 1,
    "studentNumber": "2021001",
    "username": "admin",
    "realName": "管理员",
    "englishName": "Admin",
    "gender": 1,
    "identity": 1,
    "academicStatus": null,
    "researchArea": "系统管理,用户管理",
    "phone": "13800000001",
    "email": "admin@lab.edu",
    "status": 1,
    "enrollmentYear": 2020,
    "graduationYear": null,
    "graduationDest": null,
    "resume": "系统管理员，负责实验室用户管理系统的维护和管理。",
    "homepageUrl": "https://admin.lab.edu",
    "orcid": "0000-0000-0000-0001",
    "isActive": true,
    "createTime": "2025-08-26 06:57:56",
    "updateTime": "2025-08-26 16:51:35"
  }
}
```

### 2. 分页查询用户列表
```
GET /lab/users/crud/list
```
**用途**: 管理员查看所有用户列表，支持分页和条件筛选
**权限**: 管理员
**查询参数**:
```typescript
interface UserListQuery {
  pageNum?: number;         // 页码，默认1
  pageSize?: number;        // 每页大小，默认10
  username?: string;        // 用户名筛选
  realName?: string;        // 真实姓名筛选
  englishName?: string;     // 英文名筛选
  identity?: number;        // 身份筛选：1=管理员,2=教师,3=学生
  academicStatus?: number;  // 学术身份筛选：1=教授,2=副教授,3=讲师,4=博士,5=硕士
  gender?: number;          // 性别筛选：0=未知,1=男,2=女
  status?: number;          // 状态筛选：1=在读/在职,2=毕业/离职
  isActive?: boolean;       // 是否启用
  keyword?: string;         // 关键词搜索（用户名、姓名、邮箱）
}
```
**请求示例**:
```bash
curl -X GET "http://localhost:8080/lab/users/crud/list?pageNum=1&pageSize=10&identity=2" \
  -H "Authorization: Bearer {token}"
```
**响应示例**:
```json
{
  "code": 0,
  "msg": "操作成功",
  "data": {
    "total": 6,
    "rows": [
      {
        "id": 1,
        "studentNumber": "2021001",
        "username": "admin",
        "realName": "管理员",
        "englishName": "Admin",
        "gender": 1,
        "identity": 1,
        "academicStatus": null,
        "researchArea": "系统管理,用户管理",
        "phone": "13800000001",
        "email": "admin@lab.edu",
        "status": 1,
        "isActive": true,
        "createTime": "2025-08-26 06:57:56",
        "updateTime": "2025-08-26 16:51:35"
      }
    ]
  }
}
```

### 3. 搜索用户
```
GET /lab/users/crud/search?keyword={keyword}
```
**用途**: 根据关键词模糊搜索用户（用户名、真实姓名、英文名、邮箱）
**权限**: 无需特殊权限
**查询参数**:
- `keyword`: 搜索关键词（必填）

**请求示例**:
```bash
curl -X GET "http://localhost:8080/lab/users/crud/search?keyword=张" \
  -H "Authorization: Bearer {token}"
```
**响应示例**:
```json
{
  "code": 0,
  "msg": "操作成功",
  "data": [
    {
      "id": 2,
      "username": "prof_zhang",
      "realName": "张教授",
      "englishName": "Prof. Zhang",
      "email": "zhang@lab.edu",
      "identity": 2,
      "academicStatus": 1
    }
  ]
}
```

### 4. 检查用户是否存在
```
GET /lab/users/{userId}/exists
```
**用途**: 检查指定ID的用户是否存在
**权限**: 需要登录
**路径参数**: `userId` - 用户ID
**请求示例**:
```bash
curl -X GET "http://localhost:8080/lab/users/2/exists" \
  -H "Authorization: Bearer {token}"
```
**响应示例**:
```json
{
  "code": 0,
  "msg": "操作成功",
  "data": true
}
```

### 5. 健康检查
```
GET /lab/users/test/health
```
**用途**: 检查实验室用户模块服务状态
**权限**: 无需认证
**请求示例**:
```bash
curl -X GET "http://localhost:8080/lab/users/test/health"
```
**响应示例**:
```json
{
  "code": 0,
  "msg": "操作成功",
  "data": "实验室用户模块运行正常"
}
```

### 6. 获取用户统计信息
```
GET /lab/users/crud/statistics
```
**用途**: 获取用户统计数据（按身份、性别、状态等分组统计）
**权限**: 管理员
**请求示例**:
```bash
curl -X GET "http://localhost:8080/lab/users/crud/statistics" \
  -H "Authorization: Bearer {token}"
```
**响应示例**:
```json
{
  "code": 0,
  "msg": "操作成功",
  "data": {
    "totalUsers": 6,
    "activeUsers": 5,
    "inactiveUsers": 1,
    "identityStats": {
      "admin": 1,
      "teacher": 2,
      "student": 3
    },
    "genderStats": {
      "male": 4,
      "female": 2,
      "unknown": 0
    },
    "statusStats": {
      "active": 4,
      "graduated": 2
    }
  }
}
```

## 🌐 对外开放接口（无需登录）

说明：用于实验室对外展示站点调用。仅返回公开字段，且只包含 isActive=true 且未删除（deleted=false）的成员。

### 1) 公开用户列表
- GET /open/users
- Query 参数：
  - pageNum: number，默认 1
  - pageSize: number，默认 10
  - identity: number，可选；1=管理员,2=教师,3=学生
  - academicStatus: number，可选；1=教授,2=副教授,3=讲师,4=博士,5=硕士
  - keyword: string，可选；在真实姓名/英文名/研究方向中模糊匹配
- 响应：PageDTO<PublicLabUserDTO>
- PublicLabUserDTO 字段（含邮箱）：
  - id: Long
  - realName: String
  - englishName: String
  - identity: Integer（1=管理员,2=教师,3=学生）
  - academicStatus: Integer（1=教授,2=副教授,3=讲师,4=博士,5=硕士）
  - researchArea: String
  - enrollmentYear: Year
  - graduationYear: Year
  - photo: String
  - homepageUrl: String
  - email: String（对外展示）
  - orcid: String
- 示例：
```bash
curl -s "http://localhost:8080/dev-api/open/users?pageNum=1&pageSize=12&identity=2&keyword=AI"
```

### 2) 公开用户详情
- GET /open/users/{id}
- 路径参数：id（用户ID）
- 响应：PublicLabUserDTO；若用户不存在/未启用/已删除，返回 data=null
- 示例：
```bash
curl -s "http://localhost:8080/dev-api/open/users/123"
```


## ➕ 用户创建接口

### 创建新用户
```
POST /lab/users/crud
```
**用途**: 管理员创建新用户
**权限**: 管理员
**请求数据**:
```typescript
interface CreateUserRequest {
  studentNumber?: string;     // 学号/工号
  username: string;          // 用户名（必填，唯一）
  realName: string;          // 真实姓名（必填）
  englishName?: string;      // 英文名
  password: string;          // 密码（必填，6-20位）
  gender?: number;           // 性别：0=未知,1=男,2=女
  identity: number;          // 身份（必填）：1=管理员,2=教师,3=学生
  academicStatus?: number;   // 学术身份：1=教授,2=副教授,3=讲师,4=博士,5=硕士
  researchArea?: string;     // 研究方向
  phone?: string;            // 手机号
  email?: string;            // 邮箱
  status?: number;           // 状态：1=在读/在职,2=毕业/离职
  enrollmentYear?: number;   // 入学/入职年份
  graduationYear?: number;   // 毕业/离职年份
  graduationDest?: string;   // 毕业去向
  resume?: string;           // 个人简历
  homepageUrl?: string;      // 个人主页
  orcid?: string;            // ORCID ID
  isActive?: boolean;        // 是否启用，默认true
}
```
**请求示例**:
```bash
curl -X POST "http://localhost:8080/lab/users/crud" \
  -H "Authorization: Bearer {token}" \
  -H "Content-Type: application/json" \
  -d '{
    "username": "new_student",
    "realName": "新学生",
    "englishName": "New Student",
    "password": "password123",
    "gender": 1,
    "identity": 3,
    "academicStatus": 5,
    "researchArea": "机器学习",
    "phone": "13800138000",
    "email": "newstudent@lab.edu",
    "status": 1,
    "enrollmentYear": 2024
  }'
```
**响应示例**:
```json
{
  "code": 0,
  "msg": "操作成功",
  "data": null
}
```

## ✏️ 用户更新接口

### 1. 更新个人信息
```
PUT /lab/users/crud/profile
```
**用途**: 用户更新自己的个人信息（不能修改身份、状态等敏感字段）
**权限**: 需要登录
**请求数据**:
```typescript
interface UpdateProfileRequest {
  realName: string;           // 真实姓名（必填）
  englishName?: string;       // 英文名
  gender?: number;            // 性别
  academicStatus?: number;    // 学术身份
  researchArea?: string;      // 研究方向
  phone?: string;             // 手机号
  email?: string;             // 邮箱
  graduationYear?: number;    // 毕业年份
  graduationDest?: string;    // 毕业去向
  resume?: string;            // 个人简历
  homepageUrl?: string;       // 个人主页
  orcid?: string;             // ORCID ID
}
```
**请求示例**:
```bash
curl -X PUT "http://localhost:8080/lab/users/crud/profile" \
  -H "Authorization: Bearer {token}" \
  -H "Content-Type: application/json" \
  -d '{
    "realName": "更新的姓名",
    "englishName": "Updated Name",
    "researchArea": "深度学习,计算机视觉",
    "phone": "13900139000",
    "email": "updated@lab.edu"
  }'
```
**响应示例**:
```json
{
  "code": 0,
  "msg": "操作成功",
  "data": null
}
```

### 2. 修改密码
```
PUT /lab/users/crud/password
```
**用途**: 用户修改自己的密码
**权限**: 需要登录
**请求数据**:
```typescript
interface ChangePasswordRequest {
  oldPassword: string;        // 原密码（必填）
  newPassword: string;        // 新密码（必填，6-20位）
  confirmPassword: string;    // 确认密码（必填）
}
```
**注意**: 不需要传递用户ID，后端通过JWT Token自动识别当前用户

**请求示例**:
```bash
curl -X PUT "http://localhost:8080/lab/users/crud/password" \
  -H "Authorization: Bearer {token}" \
  -H "Content-Type: application/json" \
  -d '{
    "oldPassword": "oldpass123",
    "newPassword": "newpass123",
    "confirmPassword": "newpass123"
  }'
```
**响应示例**:
```json
{
  "code": 0,
  "msg": "操作成功",
  "data": null
}
```

### 3. 管理员更新用户信息
```
PUT /lab/users/crud/{userId}
```
**用途**: 管理员更新任意用户信息（可修改所有字段包括身份、状态）
**权限**: 管理员
**路径参数**: `userId` - 要修改的用户ID

**完整请求体结构**:
```typescript
interface UpdateUserRequest {
  // 基本信息
  studentNumber?: string;      // 学号/工号
  realName?: string;          // 真实姓名
  englishName?: string;       // 英文名
  gender?: number;            // 性别：0=未知,1=男,2=女

  // 身份和状态（管理员特有权限）
  identity?: number;          // 身份：1=管理员,2=教师,3=学生
  academicStatus?: number;    // 学术身份：1=教授,2=副教授,3=讲师,4=博士,5=硕士
  status?: number;            // 状态：1=在读/在职,2=毕业/离职
  isActive?: boolean;         // 账号是否启用

  // 联系方式
  phone?: string;             // 手机号
  email?: string;             // 邮箱

  // 学术信息
  researchArea?: string;      // 研究方向
  enrollmentYear?: number;    // 入学/入职年份
  graduationYear?: number;    // 毕业/离职年份
  graduationDest?: string;    // 毕业去向

  // 个人资料
  resume?: string;            // 个人简历
  homepageUrl?: string;       // 个人主页
  orcid?: string;             // ORCID ID
}
```

**请求示例**:
```bash
curl -X PUT "http://localhost:8080/lab/users/crud/2" \
  -H "Authorization: Bearer {token}" \
  -H "Content-Type: application/json" \
  -d '{
    "realName": "张教授（已更新）",
    "identity": 2,
    "academicStatus": 1,
    "isActive": true,
    "phone": "13800138888",
    "email": "zhang.updated@lab.edu",
    "researchArea": "人工智能,深度学习"
  }'
```
**响应示例**:
```json
{
  "code": 0,
  "msg": "操作成功",
  "data": null
}
```

### 4. 管理员重置用户密码 🆕
```
PUT /lab/users/crud/{userId}/reset-password
```
**用途**: 管理员重置指定用户的密码
**权限**: 管理员
**路径参数**: `userId` - 要重置密码的用户ID
**请求数据**:
```typescript
interface ResetPasswordRequest {
  newPassword: string;        // 新密码（必填，6-20位）
  confirmPassword: string;    // 确认密码（必填）
}
```

**请求示例**:
```bash
curl -X PUT "http://localhost:8080/lab/users/crud/2/reset-password" \
  -H "Authorization: Bearer {token}" \
  -H "Content-Type: application/json" \
  -d '{
    "newPassword": "newpass123",
    "confirmPassword": "newpass123"
  }'
```
**响应示例**:
```json
{
  "code": 0,
  "msg": "操作成功",
  "data": null
}
```

**使用场景**:
- 用户忘记密码时管理员帮助重置
- 账号安全问题需要强制重置密码
- 新员工入职设置初始密码
- 批量管理时统一重置密码

## 🗑️ 用户删除接口

### 1. 删除单个用户
```
DELETE /lab/users/crud/{userId}
```
**用途**: 管理员删除用户（软删除，数据不会真正删除）
**权限**: 管理员
**路径参数**: `userId` - 要删除的用户ID

**请求示例**:
```bash
curl -X DELETE "http://localhost:8080/lab/users/crud/6" \
  -H "Authorization: Bearer {token}"
```
**响应示例**:
```json
{
  "code": 0,
  "msg": "操作成功",
  "data": null
}
```

### 2. 批量删除用户
```
DELETE /lab/users/crud/batch
```
**用途**: 管理员批量删除用户（软删除）
**权限**: 管理员
**请求数据**: `number[]` - 用户ID数组

**请求示例**:
```bash
curl -X DELETE "http://localhost:8080/lab/users/crud/batch" \
  -H "Authorization: Bearer {token}" \
  -H "Content-Type: application/json" \
  -d '[4, 5, 6]'
```
**响应示例**:
```json
{
  "code": 0,
  "msg": "操作成功",
  "data": null
}
```

## 🔐 权限说明

### 权限级别
1. **管理员**: 所有操作权限
   - 创建、删除、更新任意用户
   - 查看所有用户列表和统计信息
   - 修改用户身份和状态
   - 重置任意用户密码

2. **普通用户**: 有限权限
   - 只能查看和修改自己的信息
   - 可以搜索其他用户基本信息
   - 可以修改自己的密码

3. **公开接口**: 无需特殊权限
   - 健康检查
   - 用户搜索
   - 用户存在性检查

### 管理员特有权限字段
以下字段只有管理员可以修改：
- `studentNumber` - 学号/工号
- `identity` - 身份（管理员/教师/学生）
- `academicStatus` - 学术身份
- `status` - 在读/毕业状态
- `isActive` - 账号启用/禁用
- `enrollmentYear` - 入学年份

## 📝 数据字典

### 性别 (gender)
- `0`: 未知
- `1`: 男
- `2`: 女

### 身份 (identity)
- `1`: 管理员
- `2`: 教师
- `3`: 学生

### 学术身份 (academicStatus)
- `1`: 教授
- `2`: 副教授
- `3`: 讲师
- `4`: 博士
- `5`: 硕士

### 状态 (status)
- `1`: 在读/在职
- `2`: 毕业/离职

### 响应状态码 (code)
- `0`: 成功
- `其他`: 失败（具体错误信息见msg字段）

## 🧪 测试工具

### Swagger UI
访问地址: http://localhost:8080/swagger-ui.html
- 选择"实验室管理API"分组
- 可以直接在线测试所有接口
- 支持Token认证

### Postman Collection
文件: `实验室用户管理API.postman_collection.json`
- 包含所有接口的测试用例
- 预配置Token和测试数据

### 命令行测试脚本
文件: `快速接口测试脚本.sh`
- 自动化测试所有功能
- 快速验证接口可用性

## 🚨 常见错误码

| 错误码 | 错误信息 | 说明 |
|--------|----------|------|
| 401 | 未授权 | Token无效或已过期 |
| 403 | 权限不足 | 没有访问该接口的权限 |
| 404 | 用户不存在 | 指定的用户ID不存在 |
| 400 | 参数错误 | 请求参数格式错误或缺少必填参数 |
| 409 | 用户名已存在 | 创建用户时用户名重复 |

## 📁 代码文件位置

### 新创建的核心文件

#### 🎯 命令对象 (Commands)
1. **CreateLabUserCommand.java** - 创建用户命令
  - 位置: `agileboot-domain/src/main/java/com/agileboot/domain/lab/user/command/CreateLabUserCommand.java`

2. **UpdateLabUserCommand.java** - 管理员更新用户命令
  - 位置: `agileboot-domain/src/main/java/com/agileboot/domain/lab/user/command/UpdateLabUserCommand.java`

3. **UpdateProfileCommand.java** - 更新个人信息命令
  - 位置: `agileboot-domain/src/main/java/com/agileboot/domain/lab/user/command/UpdateProfileCommand.java`

4. **ChangePasswordCommand.java** - 修改密码命令
  - 位置: `agileboot-domain/src/main/java/com/agileboot/domain/lab/user/command/ChangePasswordCommand.java`

#### 🔍 查询对象 (Queries)
5. **LabUserQuery.java** - 用户查询对象
  - 位置: `agileboot-domain/src/main/java/com/agileboot/domain/lab/user/query/LabUserQuery.java`

#### 🏗️ 应用服务 (Application Services)
6. **LabUserCrudApplicationService.java** - CRUD应用服务
  - 位置: `agileboot-domain/src/main/java/com/agileboot/domain/lab/user/LabUserCrudApplicationService.java`

#### 🌐 控制器 (Controllers)
7. **LabUserCrudController.java** - CRUD控制器
  - 位置: `agileboot-admin/src/main/java/com/agileboot/admin/controller/lab/LabUserCrudController.java`

### 原有文件（已扩展）

#### 📊 数据访问层
8. **LabUserMapper.java** - 数据访问接口（已扩展统计方法）
  - 位置: `agileboot-domain/src/main/java/com/agileboot/domain/lab/user/db/LabUserMapper.java`

9. **LabUserMapper.xml** - MyBatis映射文件（已扩展统计SQL）
  - 位置: `agileboot-admin/src/main/resources/mapper/lab/LabUserMapper.xml`

#### 🎮 原有控制器
10. **LabUserController.java** - 原有查询控制器（保持不变）
  - 位置: `agileboot-admin/src/main/java/com/agileboot/admin/controller/lab/LabUserController.java`


## 💻 前端集成示例

### JavaScript/TypeScript
```typescript
// 基础配置
const api = axios.create({
  baseURL: 'http://localhost:8080',
  headers: {
    'Authorization': `Bearer ${token}`,
    'Content-Type': 'application/json'
  }
});

// 获取用户列表
const getUserList = async (params) => {
  const response = await api.get('/lab/users/crud/list', { params });
  return response.data;
};

// 创建用户
const createUser = async (userData) => {
  const response = await api.post('/lab/users/crud', userData);
  return response.data;
};

// 管理员重置密码
const resetPassword = async (userId, passwordData) => {
  const response = await api.put(`/lab/users/crud/${userId}/reset-password`, passwordData);
  return response.data;
};
```

### React Hook示例
```typescript
const useUserManagement = () => {
  const [users, setUsers] = useState([]);
  const [loading, setLoading] = useState(false);

  const fetchUsers = async (params = {}) => {
    setLoading(true);
    try {
      const response = await api.get('/lab/users/crud/list', { params });
      setUsers(response.data.rows);
    } catch (error) {
      console.error('获取用户列表失败:', error);
    } finally {
      setLoading(false);
    }
  };

  return { users, loading, fetchUsers };
};
```

---

**📞 技术支持**: 如有接口问题，请联系后端开发团队
**📅 文档更新**: 2025-08-27
**🔄 版本**: v1.0.0
