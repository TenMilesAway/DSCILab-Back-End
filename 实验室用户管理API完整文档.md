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
- 登录协议：沿用系统后台的协议（是否启用验证码以配置为准），密码后端使用 BCrypt 校验
- 用户来源（可配置）：
  - 通过配置 `agileboot.auth.use-lab-only=true`（默认开启）可强制仅使用 `lab_user` 进行登录与鉴权；关闭时会先查 `sys_user`，再兜底 `lab_user`
  - `lab_user` 登录要求：`is_active=true` 且 `deleted=false`，密码为 BCrypt 存储
- 权限与角色（按 lab_user.identity 映射）：
  - 1 → `lab:admin`（默认 `*:*:*` 全权限）
  - 2 → `lab:teacher`（教学/成果主要权限集）
  - 3 → `lab:student`（基础使用权限集）
- 登录成功返回的用户信息（lab-only 模式）：

  {
    "id": "number",
    "username": "string",
    "realName": "string",
    "gender": "number (0未知 1男 2女)",
    "genderDesc": "string",
    "identity": "number (1管理员 2教师 3学生)",
    "email": "string",
    "phone": "string",
    "photo": "string",
    "isActive": "boolean",
    "createTime": "string",
    "updateTime": "string"
  }

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
- Query 字段
  {
    "pageNum": {"type": "number", "default": 1},
    "pageSize": {"type": "number", "default": 1000},
    "identity": {"type": "number", "remark": "1管理员 2教师 3学生"},
    "academicStatus": {"type": "number", "remark": "1..5"},
    "keyword": {"type": "string"}
  }
| 公开用户详情 | GET | `/open/users/{id}` | 公开 | 返回公开成员详情（含邮箱）
- Path 字段
  { "id": {"type": "number", "required": true} }

### 🏆 成果管理接口
| 接口 | 方法 | 路径 | 权限 | 说明 |
|------|------|------|------|------|
| 成果列表 | GET | `/lab/achievements` | 管理员/教师 | 分页查询成果列表
| 创建成果 | POST | `/lab/achievements` | 管理员/教师 | 创建新成果
| 成果详情 | GET | `/lab/achievements/{id}` | 管理员/教师 | 获取成果详情
| 更新成果 | PUT | `/lab/achievements/{id}` | 管理员/拥有者 | 更新成果信息
| 删除成果 | DELETE | `/lab/achievements/{id}` | 管理员/拥有者 | 删除成果
| 发布成果 | PUT | `/lab/achievements/{id}/publish` | 管理员 | 发布/取消发布
| 审核成果 | PUT | `/lab/achievements/{id}/verify` | 管理员 | 审核通过/取消审核
| 作者列表 | GET | `/lab/achievements/{id}/authors` | 管理员/教师 | 获取成果作者
| 添加作者 | POST | `/lab/achievements/{id}/authors` | 管理员/拥有者 | 添加作者
| 更新作者 | PUT | `/lab/achievements/{id}/authors/{authorId}` | 管理员/拥有者 | 更新作者信息
| 删除作者 | DELETE | `/lab/achievements/{id}/authors/{authorId}` | 管理员/拥有者 | 删除作者
| 我的成果 | GET | `/lab/my-achievements` | 教师/学生 | 我参与的成果列表
| 切换可见性 | PUT | `/lab/my-achievements/{id}/visibility` | 教师 | 切换个人页可见性
| 公开成果列表 | GET | `/open/achievements` | 公开 | 对外展示成果列表
| 公开成果详情 | GET | `/open/achievements/{id}` | 公开 | 对外展示成果详情
- Path 字段
  { "id": {"type": "number", "required": true} }
| 公开成果列表 | GET | `/open/achievements` | 公开 | 对外展示成果列表
- Query 字段
  {
    "pageNum": {"type": "number", "default": 1},
    "pageSize": {"type": "number", "default": 10},
    "keyword": {"type": "string"},
    "type": {"type": "number", "remark": "1论文 2项目"}
  }
| 用户成果列表 | GET | `/open/users/{userId}/achievements` | 公开 | 某用户的公开成果
- Path/Query 字段
  {
    "path": { "userId": {"type": "number", "required": true} },
    "query": { "pageNum": {"type": "number", "default": 1}, "pageSize": {"type": "number", "default": 10} }
  }
| 成果类型字典 | GET | `/lab/dicts/achievement-types` | 公开 | 成果类型字典
| 论文类型字典 | GET | `/lab/dicts/paper-types` | 公开 | 论文子类型字典
| 项目类型字典 | GET | `/lab/dicts/project-types` | 公开 | 项目子类型字典




#### 参数说明与测试用例（成果管理）

注意：需要在请求头中携带 Authorization: Bearer <token>。POST/PUT 接口必须携带 Content-Type: application/json 和合法 JSON 请求体，否则会报 “Required request body is missing”。

1) 列表：GET /lab/achievements
- Query 参数（均可选，分页有默认值）
  {
    "pageNum": {"type": "number", "default": 1},
    "pageSize": {"type": "number", "default": 10},
    "keyword": {"type": "string"},
    "type": {"type": "number", "remark": "1论文 2项目"},
    "paperType": {"type": "number"},
    "projectType": {"type": "number"},
    "published": {"type": "boolean"},
    "isVerified": {"type": "boolean"},
    "ownerUserId": {"type": "number"},
    "dateStart": {"type": "string", "remark": "yyyy-MM-dd（论文按publish_date；项目按project_start_date）"},
    "dateEnd": {"type": "string", "remark": "yyyy-MM-dd"}
  }
- 测试用例
  curl -i -H "Authorization: Bearer <token>" "http://localhost:8080/lab/achievements?pageNum=1&pageSize=20&type=1"

2) 创建：POST /lab/achievements
- Headers: Content-Type: application/json
- Body 字段（已更新日期格式约定：publishDate=yyyy；project*=yyyy-MM）
  {
    "title": {"type": "string", "required": true},
    "titleEn": {"type": "string"},
    "description": {"type": "string"},
    "keywords": {"type": "string"},
    "type": {"type": "number", "required": true, "remark": "1论文 2项目"},
    "paperType": {"type": "number"},
    "projectType": {"type": "number"},
    "venue": {"type": "string"},
    "publishDate": {"type": "string", "remark": "yyyy（仅论文）"},
    "projectStartDate": {"type": "string", "remark": "yyyy-MM（仅项目）"},
    "projectEndDate": {"type": "string", "remark": "yyyy-MM（仅项目）"},
    "coverUrl": {"type": "string"},
    "linkUrl": {"type": "string"},
    "gitUrl": {"type": "string"},
    "homepageUrl": {"type": "string"},
    "pdfUrl": {"type": "string"},
    "doi": {"type": "string"},
    "fundingAmount": {"type": "number", "remark": ">=0，单位：万元"},
    "published": {"type": "boolean", "default": false},
    "extra": {"type": "string"},
    "authors": {"type": "array", "items": {
      "userId": {"type": "number", "remark": "内部作者userId；外部作者留空"},
      "name": {"type": "string", "remark": "外部作者必填"},
      "nameEn": {"type": "string"},
      "affiliation": {"type": "string"},
      "authorOrder": {"type": "number", "required": true},
      "isCorresponding": {"type": "boolean"},
      "role": {"type": "string"},
      "visible": {"type": "boolean"}
    }}
  }
- 校验要点
  - type=1：应提供 paperType、publishDate；project* 为空
  - type=2：应提供 projectType、projectStartDate；publishDate 为空
- 测试用例（论文）
  curl -i -X POST "http://localhost:8080/lab/achievements" \
    -H "Authorization: Bearer <token>" \
    -H "Content-Type: application/json" \
    -d '{"title":"AutoTest-Paper","type":1,"paperType":1,"venue":"Test Journal","publishDate":"2024","published":false}'

3) 详情：GET /lab/achievements/{id}
- Path 参数：id (Long)
- 测试用例
  curl -i -H "Authorization: Bearer <token>" "http://localhost:8080/lab/achievements/123"

4) 更新：PUT /lab/achievements/{id}
- Headers: Content-Type: application/json
- Body 字段：同“创建”字段；遵循同样的类型/互斥校验
- 权限与审核说明
  - 拥有者或管理员可更新；普通作者是否可改由策略控制（当前为拥有者/管理员）
  - isVerified=true 的成果仅管理员可改关键字段（类型/时间/doi 等）
- 测试用例（论文）
  curl -i -X PUT "http://localhost:8080/lab/achievements/123" \
    -H "Authorization: Bearer <token>" \
    -H "Content-Type: application/json" \
    -d '{"title":"AutoTest-Paper(Updated)","type":1,"paperType":2,"venue":"Test Conf","publishDate":"2024-07-01","published":false}'

5) 删除：DELETE /lab/achievements/{id}
- 说明：软删除；管理员或拥有者可删
- 测试用例
  curl -i -X DELETE "http://localhost:8080/lab/achievements/123" -H "Authorization: Bearer <token>"

6) 发布：PUT /lab/achievements/{id}/publish
- Query 参数：published=true/false
- 说明：仅管理员可操作
- 测试用例
  curl -i -X PUT "http://localhost:8080/lab/achievements/123/publish?published=true" -H "Authorization: Bearer <token>"

7) 审核：PUT /lab/achievements/{id}/verify

8) 作者管理接口

8.1 作者列表：GET /lab/achievements/{achievementId}/authors
- 权限：lab:achievement:query
- 路径参数：achievementId(Long)
- 响应：作者数组（按 authorOrder 升序）
- 示例（Reqable 原始请求）
GET http://localhost:8080/lab/achievements/{achievementId}/authors
Authorization: Bearer {{token}}
Accept: application/json


- 作者DTO字段说明（列表/详情返回使用）
{
  "id": Long,                 // 作者记录ID
  "achievementId": Long,     // 成果ID
  "userId": Long|null,       // 内部作者用户ID；外部作者为null
  "name": String|null,       // 作者姓名（外部作者必填；内部作者可冗余）
  "nameEn": String|null,     // 英文姓名
  "affiliation": String|null,// 单位/机构
  "authorOrder": Integer,    // 作者顺序(>0)
  "isCorresponding": Boolean,// 是否通讯作者
  "role": String|null,       // 作者角色/贡献
  "visible": Boolean,        // 仅内部作者生效：是否在个人页可见
  "isInternal": Boolean,     // 是否内部作者（userId!=null）
  "createTime": string,      // 创建时间（ISO或服务端默认格式）
  "updateTime": string       // 更新时间
}


8.2 新增作者：POST /lab/achievements/{achievementId}/authors
- 权限：lab:achievement:edit（管理员或成果拥有者）
- 路径参数：achievementId(Long)
- Body(JSON)：
{
  "userId": 5,                  // 内部作者userId；为空表示外部作者
  "name": "John Doe",          // 外部作者必填；内部作者可选（冗余展示）
  "nameEn": "John Doe",        // 可选
  "affiliation": "MIT",        // 可选
  "authorOrder": 1,             // 必填，>0，成果内唯一
  "isCorresponding": false,     // 可选，默认false
  "role": "第一作者",          // 可选
  "visible": true               // 可选，仅内部作者生效
}
- 示例（Reqable 原始请求）
POST http://localhost:8080/lab/achievements/{achievementId}/authors
Authorization: Bearer {{token}}
Content-Type: application/json
Accept: application/json

{
  "userId": 5,
  "authorOrder": 1,
  "isCorresponding": false,
  "role": "第一作者",
  "visible": true
}

外部作者示例：
POST http://localhost:8080/lab/achievements/{achievementId}/authors
Authorization: Bearer {{token}}
Content-Type: application/json
Accept: application/json

{
  "name": "John Doe",
  "nameEn": "John Doe",
  "affiliation": "MIT",
  "authorOrder": 2,
  "isCorresponding": true,
  "role": "共同一作",
  "visible": true
}


8.3 更新作者：PUT /lab/achievements/{achievementId}/authors/{authorId}
- 权限：lab:achievement:edit（管理员或成果拥有者）
- 路径参数：achievementId(Long), authorId(Long)
- Body(JSON)：（全部可选，按需传入，若传 authorOrder 需保证唯一）
{
  "userId": 6,
  "name": "John A. Doe",
  "nameEn": "John A. Doe",
  "affiliation": "MIT CSAIL",
  "authorOrder": 3,
  "isCorresponding": false,
  "role": "通讯作者",
  "visible": true
}
- 示例（Reqable 原始请求）
PUT http://localhost:8080/lab/achievements/{achievementId}/authors/{authorId}
Authorization: Bearer {{token}}
Content-Type: application/json
Accept: application/json

{
  "affiliation": "MIT CSAIL",
  "authorOrder": 3,
  "isCorresponding": false,
  "role": "通讯作者"
}


9) 用户自助端接口

9.1 我的成果列表：GET /lab/my-achievements
- 权限：lab:achievement:query
- Query 字段（均可选，分页有默认值）
  {
    "pageNum": {"type": "number", "default": 1},
    "pageSize": {"type": "number", "default": 10},
    "keyword": {"type": "string"},
    "type": {"type": "number", "remark": "1论文 2项目"},
    "paperType": {"type": "number"},
    "projectType": {"type": "number"},
    "published": {"type": "boolean"},
    "isVerified": {"type": "boolean"},
    "dateStart": {"type": "string", "remark": "yyyy-MM-dd（论文按publish_date；项目按project_start_date）"},
    "dateEnd": {"type": "string", "remark": "yyyy-MM-dd"}
  }
- 说明：返回我拥有的成果 + 我作为作者参与的成果
- 示例（Reqable 原始请求）
GET http://localhost:8080/lab/my-achievements?pageNum=1&pageSize=20&type=1
Authorization: Bearer {{token}}
Accept: application/json


9.2 切换个人页可见性：PUT /lab/my-achievements/{achievementId}/visibility
- 权限：lab:achievement:edit
- 路径/Query 字段
  {
    "path": { "achievementId": {"type": "number", "required": true} },
    "query": { "visible": {"type": "boolean", "required": true} }
  }
- 说明：仅作者本人可切换自己在该成果中的个人页可见性
- 示例（Reqable 原始请求）
PUT http://localhost:8080/lab/my-achievements/{achievementId}/visibility?visible=true
Authorization: Bearer {{token}}
Accept: application/json
8.4 删除作者（软删）：DELETE /lab/achievements/{achievementId}/authors/{authorId}
- 权限：lab:achievement:edit（管理员或成果拥有者）
- 路径参数：achievementId(Long), authorId(Long)
- 示例（Reqable 原始请求）
DELETE http://localhost:8080/lab/achievements/{achievementId}/authors/{authorId}
Authorization: Bearer {{token}}
Accept: application/json


8.5 调整作者顺序：PUT /lab/achievements/{achievementId}/authors/{authorId}/reorder
- 权限：lab:achievement:edit（管理员或成果拥有者）
- 路径参数：achievementId(Long), authorId(Long)
- Query：newOrder(>=1)
- 示例（Reqable 原始请求）
PUT http://localhost:8080/lab/achievements/{achievementId}/authors/{authorId}/reorder?newOrder=2
Authorization: Bearer {{token}}
Accept: application/json


8.6 切换个人页可见性：PUT /lab/achievements/{achievementId}/authors/{authorId}/visibility
- 权限：lab:achievement:edit（管理员或成果拥有者；仅内部作者有效）
- 路径参数：achievementId(Long), authorId(Long)
- Query：visible=true/false
- 示例（Reqable 原始请求）
PUT http://localhost:8080/lab/achievements/{achievementId}/authors/{authorId}/visibility?visible=true
Authorization: Bearer {{token}}
Accept: application/json

- Query 参数：verified=true/false
- 说明：仅管理员可操作；审核通过后关键字段受限
- 测试用例
  curl -i -X PUT "http://localhost:8080/lab/achievements/123/verify?verified=true" -H "Authorization: Bearer <token>"

提示
- 若经网关，请将 URL 前缀改为 /dev-api，例如 http://localhost:8080/dev-api/lab/achievements
- 常见错误 “Required request body is missing” 多因未携带 Content-Type: application/json 或缺少 JSON Body 导致。

### ➕ 创建接口
| 接口 | 方法 | 路径 | 权限 | 说明 |
|------|------|------|------|------|
| 创建新用户 | POST | `/lab/users/crud` | 管理员 | 创建实验室用户 |
- Body(JSON)
  {
    "studentNumber": {"type": "string"},
    "username": {"type": "string", "required": true},
    "realName": {"type": "string", "required": true},
    "englishName": {"type": "string"},
    "password": {"type": "string", "required": true},
    "gender": {"type": "number", "required": true, "remark": "0未知 1男 2女"},
    "identity": {"type": "number", "required": true, "remark": "1管理员 2教师 3学生"},
    "academicStatus": {"type": "number"},
    "researchArea": {"type": "string"},
    "phone": {"type": "string"},
    "email": {"type": "string"},
    "status": {"type": "number"},
    "enrollmentYear": {"type": "number"},
    "graduationYear": {"type": "number"},
    "graduationDest": {"type": "string"},
    "resume": {"type": "string"},
    "homepageUrl": {"type": "string"},
    "orcid": {"type": "string"},
    "isActive": {"type": "boolean", "default": true}
  }

### ✏️ 更新接口
| 接口 | 方法 | 路径 | 权限 | 说明 |
|------|------|------|------|------|
| 更新个人信息 | PUT | `/lab/users/crud/profile` | 登录用户 | 修改自己的信息 |
- Body(JSON)
  {
    "realName": {"type": "string"},
    "englishName": {"type": "string"},
    "gender": {"type": "number"},
    "phone": {"type": "string"},
    "email": {"type": "string"},
    "researchArea": {"type": "string"},
    "homepageUrl": {"type": "string"},
    "resume": {"type": "string"}
  }
| 修改密码 | PUT | `/lab/users/crud/password` | 登录用户 | 修改登录密码 |
- Body(JSON)
  {
    "oldPassword": {"type": "string", "required": true},
    "newPassword": {"type": "string", "required": true}
  }
| 管理员更新用户 | PUT | `/lab/users/crud/{id}` | 管理员 | 修改任意用户信息 |
- Body(JSON)
  {
    "studentNumber": {"type": "string"},
    "realName": {"type": "string", "required": true},
    "englishName": {"type": "string"},
    "gender": {"type": "number"},
    "identity": {"type": "number"},
    "academicStatus": {"type": "number"},
    "researchArea": {"type": "string"},
    "phone": {"type": "string"},
    "email": {"type": "string"},
    "status": {"type": "number"},
    "enrollmentYear": {"type": "number"},
    "graduationYear": {"type": "number"},
    "graduationDest": {"type": "string"},
    "resume": {"type": "string"},
    "homepageUrl": {"type": "string"},
    "orcid": {"type": "string"},
    "isActive": {"type": "boolean"}
  }

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
  academicStatus?: number;  // 学术身份筛选：0=实验室负责人,1=教授,2=副教授,3=讲师,4=博士,5=硕士,6=本科
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
  academicStatus?: number;   // 学术身份：0=实验室负责人,1=教授,2=副教授,3=讲师,4=博士,5=硕士,6=本科
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
  academicStatus?: number;    // 学术身份：0=实验室负责人,1=教授,2=副教授,3=讲师,4=博士,5=硕士,6=本科
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
  academicStatus?: number;    // 学术身份：0=实验室负责人,1=教授,2=副教授,3=讲师,4=博士,5=硕士,6=本科
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
- `0`: 实验室负责人
- `1`: 教授
- `2`: 副教授
- `3`: 讲师
- `4`: 博士
- `5`: 硕士
- `6`: 本科

---

## 🏆 成果管理模块开发进度

### ✅ 已完成的部分

#### 1. 数据库层面
- ✅ 创建了 `lab_achievement` 和 `lab_achievement_author` 表
- ✅ 包含完整的字段、索引和 CHECK 约束
- ✅ 支持论文/项目分类，内外部作者统一管理
- ✅ 时间字段区分：论文用 `publish_date`，项目用 `project_start_date/project_end_date`
- ✅ 新增字段：git_url、homepage_url、pdf_url、doi、funding_amount、extra

#### 2. 字典接口
- ✅ 扩展了 `LabDictController`，新增：
  - GET `/lab/dicts/achievement-types` - 成果类型字典
  - GET `/lab/dicts/paper-types` - 论文类型字典
  - GET `/lab/dicts/project-types` - 项目类型字典
- ✅ 更新了 API 文档

#### 3. Domain 层（完整）
- ✅ `LabAchievementEntity` - 成果实体（包含完整枚举）
- ✅ `LabAchievementAuthorEntity` - 作者实体
- ✅ `LabAchievementMapper` / `LabAchievementAuthorMapper` - 数据访问层
- ✅ `LabAchievementService` / `LabAchievementAuthorService` - 服务层
- ✅ `LabAchievementDTO` / `LabAchievementAuthorDTO` - 数据传输对象
- ✅ `CreateLabAchievementCommand` - 创建命令
- ✅ `LabAchievementQuery` - 查询条件
- ✅ `LabAchievementApplicationService` - 应用服务层（部分完成）

#### 4. Controller 层（基础框架）
- ✅ `LabAchievementController` - 管理员端控制器
- ✅ 使用现有权限注解 `@permission.has`
- ✅ 遵循现有 `@AccessLog` 访问日志模式
- ✅ 已实现：列表查询、详情查询、创建成果

### 🔄 遵循的原有框架模式
1. **认证授权**：复用 JWT + Spring Security + 权限注解
2. **分层架构**：Entity -> Mapper -> Service -> ApplicationService -> Controller
3. **数据传输**：Command/Query/DTO 模式
4. **异常处理**：使用 `ApiException` 和 `ErrorCode`
5. **分页查询**：使用 `PageDTO` 和 `AbstractPageQuery`
6. **参数校验**：使用 `@Validated` 和 JSR-303 注解

### 📋 下一步工作计划

#### Phase 1: 完善核心 CRUD（✅ 已完成）
1. **完善 ApplicationService**：
   - ✅ 创建成果逻辑
   - ✅ 更新成果逻辑（含审核锁定检查）
   - ✅ 删除成果逻辑（软删除）
   - ✅ 发布/审核逻辑（仅管理员）
   - ✅ 权限检查逻辑（拥有者/作者/管理员）
   - ✅ 为 lab 普通成员添加成果权限：query、list、add、edit

#### Phase 2: 作者管理
2. **作者管理接口**：
   - ⏳ 作者 CRUD 操作
   - ⏳ 可见性控制
   - ⏳ 内外部作者统一管理

#### Phase 3: 用户自助功能
3. **用户自助接口**：
   - ⏳ 我的成果列表
   - ⏳ 个人可见性控制（仅教师）

#### Phase 4: 对外展示
4. **公开展示接口**：
   - ⏳ 对外成果列表
   - ⏳ 用户成果页面
   - ⏳ 筛选和搜索功能

#### Phase 5: 权限完善
5. **权限配置**：
   - ⏳ 为 lab 普通成员添加成果相关权限到默认权限集
   - ⏳ 细化权限控制（拥有者/作者/管理员）

### 🎯 当前优先级（更新）
✅ **Phase 1 已完成**，下一个任务是 **Phase 2: 作者管理**，包括作者 CRUD 操作和可见性控制。

### 📊 当前可用功能
- ✅ 成果管理基础 CRUD（创建、查询、更新、删除）
- ✅ 发布/审核控制（仅管理员）
- ✅ 权限控制（管理员全权限，普通用户可编辑自己的成果）
- ✅ 审核锁定机制（已审核成果限制修改关键字段）
- ✅ 字典接口（成果类型、论文类型、项目类型）
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


## ✏️ 用户更新接口

### 1. 更新个人信息
```
PUT /lab/users/crud/profile
```
用途: 用户更新自己的个人信息（不能修改身份、状态等敏感字段）
权限: 需要登录
请求数据:
```typescript
interface UpdateProfileRequest {
  realName: string;           // 真实姓名（必填）
  englishName?: string;       // 英文名
  gender?: number;            // 性别：0=未知,1=男,2=女
  academicStatus?: number;    // 学术身份：0负责人,1教授,2副教授,3讲师,4博士,5硕士,6本科
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
请求示例:
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

### 2. 修改密码
```
PUT /lab/users/crud/password
```
用途: 用户自助修改登录密码
权限: 需要登录
请求数据:
```typescript
interface ChangePasswordRequest {
  oldPassword: string;  // 旧密码（必填）
  newPassword: string;  // 新密码（必填，6-20位）
}
```
请求示例:
```bash
curl -X PUT "http://localhost:8080/lab/users/crud/password" \
  -H "Authorization: Bearer {token}" \
  -H "Content-Type: application/json" \
  -d '{
    "oldPassword": "Passw0rd",
    "newPassword": "Passw0rd2"
  }'
```

### 3. 管理员重置用户密码
```
PUT /lab/users/crud/{userId}/password
```
用途: 管理员为指定用户重置密码（无需旧密码）
权限: 管理员（lab:admin 或 lab:user:edit）
请求数据:
```typescript
interface ResetPasswordByAdminRequest {
  password: string;   // 新密码（必填，6-20位）
}
```
请求示例:
```bash
curl -X PUT "http://localhost:8080/lab/users/crud/1/password" \
  -H "Authorization: Bearer {token}" \
  -H "Content-Type: application/json" \
  -d '{
    "password": "Passw0rd"
  }'
```

### 4. 管理员更新用户信息
```
PUT /lab/users/crud/{userId}
```
用途: 管理员更新任意用户的信息
权限: 管理员（lab:admin 或 lab:user:edit）
请求数据:
```typescript
interface UpdateLabUserRequest {
  studentNumber?: string;
  realName: string;            // 真实姓名（必填）
  englishName?: string;
  gender?: number;             // 0未知 1男 2女
  identity?: number;           // 1管理员 2教师 3学生
  academicStatus?: number;     // 0负责人 1教授 2副教授 3讲师 4博士 5硕士 6本科
  researchArea?: string;
  phone?: string;
  email?: string;
  status?: number;             // 1在读/在职 2毕业/离职
  enrollmentYear?: number;
  graduationYear?: number;
  graduationDest?: string;
  resume?: string;
  homepageUrl?: string;
  orcid?: string;
  isActive?: boolean;
}
```
请求示例:
```bash
curl -X PUT "http://localhost:8080/lab/users/crud/2" \
  -H "Authorization: Bearer {token}" \
  -H "Content-Type: application/json" \
  -d '{
    "realName": "张三丰",
    "identity": 2,
    "isActive": true
  }'
```

## ➕ 用户创建与删除接口

### 5. 创建用户（管理员）
```
POST /lab/users/crud
```
用途: 创建实验室用户
权限: 管理员（lab:admin 或 lab:user:add）
请求数据:
```typescript
interface CreateLabUserRequest {
  studentNumber?: string;
  username: string;           // 必填，3-50
  realName: string;           // 必填
  englishName?: string;
  password: string;           // 必填，6-20
  gender: number;             // 必填：0未知 1男 2女
  identity: number;           // 必填：1管理员 2教师 3学生
  academicStatus?: number;    // 0负责人 1教授 2副教授 3讲师 4博士 5硕士 6本科
  researchArea?: string;
  phone?: string;             // 中国大陆手机号格式校验
  email?: string;
  status?: number;            // 1在读/在职 2毕业/离职
  enrollmentYear?: number;
  graduationYear?: number;
  graduationDest?: string;
  resume?: string;
  homepageUrl?: string;
  orcid?: string;
  isActive?: boolean;         // 默认 true
}
```
请求示例:
```bash
curl -X POST "http://localhost:8080/lab/users/crud" \
  -H "Authorization: Bearer {token}" \
  -H "Content-Type: application/json" \
  -d '{
    "username": "user1",
    "realName": "张三",
    "password": "Passw0rd",
    "gender": 1,
    "identity": 3,
    "isActive": true
  }'
```

### 6. 删除用户（管理员）
```
DELETE /lab/users/crud/{userId}
```
用途: 管理员删除指定用户（硬删除）
权限: 管理员（lab:admin 或 lab:user:remove）
请求示例:
```bash
curl -X DELETE "http://localhost:8080/lab/users/crud/2" \
  -H "Authorization: Bearer {token}"
```

### 7. 批量删除用户（管理员）
```
DELETE /lab/users/crud/batch
```
用途: 管理员批量删除用户（硬删除）
权限: 管理员（lab:admin 或 lab:user:remove）
请求数据:
```json
[1,2,3]
```
请求示例:
```bash
curl -X DELETE "http://localhost:8080/lab/users/crud/batch" \
  -H "Authorization: Bearer {token}" \
  -H "Content-Type: application/json" \
  -d '[1,2,3]'
```


## 🔍 用户查询与存在性接口

### 8. 获取当前登录用户信息
```
GET /lab/users/profile
```
用途: 获取当前登录用户的详细个人信息
权限: 需要登录
请求示例:
```bash
curl -X GET "http://localhost:8080/lab/users/profile" \
  -H "Authorization: Bearer {token}"
```

### 9. 根据用户ID获取个人信息
```
GET /lab/users/{userId}
```
用途: 管理员可查看任何用户；普通用户仅能查看自己
权限: 管理员或本人
路径参数:
```json
{ "userId": {"type": "number", "required": true} }
```
请求示例:
```bash
curl -X GET "http://localhost:8080/lab/users/1" -H "Authorization: Bearer {token}"
```

### 10. 根据用户名获取个人信息
```
GET /lab/users/username/{username}
```
用途: 通过用户名获取个人信息
权限: 管理员或本人
路径参数:
```json
{ "username": {"type": "string", "required": true} }
```

### 11. 检查用户是否存在
```
GET /lab/users/{userId}/exists
```
用途: 检查指定ID的用户是否存在
权限: 登录
路径参数:
```json
{ "userId": {"type": "number", "required": true} }
```

### 12. 检查用户名是否存在
```
GET /lab/users/username/{username}/exists
```
用途: 检查用户名是否已存在
权限: 登录
路径参数:
```json
{ "username": {"type": "string", "required": true} }
```

### 13. 分页查询用户列表
```
GET /lab/users/crud/list
```
用途: 管理端分页查询用户，支持多条件筛选
权限: 管理员（lab:admin 或 lab:user:list）
查询参数:
```json
{
  "pageNum": {"type": "number", "default": 1},
  "pageSize": {"type": "number", "default": 10},
  "username": {"type": "string"},
  "realName": {"type": "string"},
  "englishName": {"type": "string"},
  "identity": {"type": "number"},
  "academicStatus": {"type": "number"},
  "researchAreaKeyword": {"type": "string"},
  "email": {"type": "string"},
  "phone": {"type": "string"},
  "status": {"type": "number"},
  "enrollmentYearStart": {"type": "number"},
  "enrollmentYearEnd": {"type": "number"},
  "isActive": {"type": "boolean"},
  "keyword": {"type": "string"}
}
```

### 14. 关键词搜索用户
```
GET /lab/users/crud/search?keyword=xxx
```
用途: 关键词搜索（姓名/用户名/邮箱）
权限: 管理员
查询参数:
```json
{ "keyword": {"type": "string", "required": true} }
```

---

## 🏆 成果管理接口（逐条）

### 1. 成果列表
```
GET /lab/achievements
```
用途: 分页查询成果列表
权限: 管理员/教师
查询参数:
```json
{
  "pageNum": {"type": "number", "default": 1},
  "pageSize": {"type": "number", "default": 10},
  "keyword": {"type": "string"},
  "type": {"type": "number", "remark": "1论文 2项目"},
  "paperType": {"type": "number"},
  "projectType": {"type": "number"},
  "published": {"type": "boolean"},
  "isVerified": {"type": "boolean"},
  "ownerUserId": {"type": "number"},
  "dateStart": {"type": "string", "remark": "yyyy-MM-dd（论文按publish_date；项目按project_start_date）"},
  "dateEnd": {"type": "string", "remark": "yyyy-MM-dd"}
}
```

### 2. 成果详情
```
GET /lab/achievements/{id}
```
用途: 获取成果详情
权限: 管理员/教师
路径参数:
```json
{ "id": {"type": "number", "required": true} }
```

### 3. 创建成果
```
POST /lab/achievements
```
用途: 创建新成果（论文/项目）
权限: 管理员/教师
请求数据:
```typescript
interface CreateAchievementRequest {
  title: string;                 // 必填
  titleEn?: string;
  description?: string;
  keywords?: string;
  type: number;                  // 必填：1论文 2项目
  paperType?: number;            // 仅当 type=1 时使用
  projectType?: number;          // 仅当 type=2 时使用
  venue?: string;
  publishDate?: string;          // yyyy（论文）
  projectStartDate?: string;     // yyyy-MM（项目）
  projectEndDate?: string;       // yyyy-MM（项目）
  coverUrl?: string;
  linkUrl?: string;
  gitUrl?: string;
  homepageUrl?: string;
  pdfUrl?: string;
  doi?: string;
  fundingAmount?: number;        // >=0，单位：万元
  published?: boolean;           // 默认 false
  extra?: string;
  authors?: AuthorItem[];        // 可选
}
interface AuthorItem {
  userId?: number;               // 内部作者userId；外部作者留空
  name?: string;                 // 外部作者必填
  nameEn?: string;
  affiliation?: string;
  authorOrder: number;           // 必填，>=1
  isCorresponding?: boolean;     // 默认 false
  role?: string;
  visible?: boolean;             // 内部作者适用
}
```

### 4. 更新成果
```
PUT /lab/achievements/{id}
```
用途: 更新成果信息
权限: 管理员/拥有者
路径参数:
```json
{ "id": {"type": "number", "required": true} }
```
请求数据: 同“创建成果”字段

### 5. 删除成果
```
DELETE /lab/achievements/{id}
```
用途: 删除成果（软删）
权限: 管理员/拥有者

### 6. 发布/取消发布
```
PUT /lab/achievements/{id}/publish?published=true|false
```
用途: 设置成果发布状态
权限: 管理员
路径/查询参数:
```json
{ "path": {"id": {"type": "number", "required": true}}, "query": {"published": {"type": "boolean", "required": true}} }
```

### 7. 审核/取消审核
```
PUT /lab/achievements/{id}/verify?verified=true|false
```
用途: 设置成果审核状态
权限: 管理员
路径/查询参数:
```json
{ "path": {"id": {"type": "number", "required": true}}, "query": {"verified": {"type": "boolean", "required": true}} }
```

### 8. 作者列表
```
GET /lab/achievements/{achievementId}/authors
```
用途: 获取成果作者
权限: 管理员/教师
路径参数:
```json
{ "achievementId": {"type": "number", "required": true} }
```

### 9. 新增作者
```
POST /lab/achievements/{achievementId}/authors
```
用途: 向成果添加作者（内部/外部）
权限: 管理员/拥有者
请求数据:
```typescript
interface AddAuthorRequest {
  userId?: number;           // 内部作者
  name?: string;             // 外部作者必填
  nameEn?: string;
  affiliation?: string;
  authorOrder: number;       // 必填，>=1
  isCorresponding?: boolean;
  role?: string;
  visible?: boolean;
}
```

### 10. 更新作者
```
PUT /lab/achievements/{achievementId}/authors/{authorId}
```
用途: 更新作者信息
权限: 管理员/拥有者
路径参数:
```json
{ "achievementId": {"type": "number", "required": true}, "authorId": {"type": "number", "required": true} }
```
请求数据: 与新增作者相同（均可选）

### 11. 删除作者
```
DELETE /lab/achievements/{achievementId}/authors/{authorId}
```
用途: 删除作者（软删）
权限: 管理员/拥有者

### 12. 调整作者顺序
```
PUT /lab/achievements/{achievementId}/authors/{authorId}/reorder?newOrder=2
```
用途: 调整作者顺序
权限: 管理员/拥有者
查询参数:
```json
{ "newOrder": {"type": "number", "required": true} }
```

### 13. 切换作者可见性
```
PUT /lab/achievements/{achievementId}/authors/{authorId}/visibility?visible=true|false
```
用途: 切换作者在个人页是否可见（仅内部作者生效）
权限: 管理员/拥有者
查询参数:
```json
{ "visible": {"type": "boolean", "required": true} }
```

---

## 👤 我的成果（自助端）

### 1. 我的成果列表
```
GET /lab/my-achievements
```
用途: 获取我拥有或参与的成果列表
权限: 登录（lab:achievement:query）
查询参数:
```json
{
  "pageNum": {"type": "number", "default": 1},
  "pageSize": {"type": "number", "default": 10},
  "keyword": {"type": "string"},
  "type": {"type": "number"},
  "paperType": {"type": "number"},
  "projectType": {"type": "number"},
  "published": {"type": "boolean"},
  "isVerified": {"type": "boolean"},
  "dateStart": {"type": "string", "remark": "yyyy-MM-dd"},
  "dateEnd": {"type": "string", "remark": "yyyy-MM-dd"}
}
```

### 2. 切换我的可见性
```
PUT /lab/my-achievements/{achievementId}/visibility?visible=true|false
```
用途: 切换我在指定成果中的可见性
权限: 登录（作者本人）
路径/查询参数:
```json
{ "path": {"achievementId": {"type": "number", "required": true}}, "query": {"visible": {"type": "boolean", "required": true}} }
```

---

## 🌐 公开接口

### 1. 公开成果列表
```
GET /open/achievements
```
用途: 对外展示成果列表（仅已发布且已审核）
权限: 公开
查询参数:
```json
{
  "pageNum": {"type": "number", "default": 1},
  "pageSize": {"type": "number", "default": 10},
  "keyword": {"type": "string"},
  "type": {"type": "number"}
}
```

### 2. 公开成果详情
```
GET /open/achievements/{id}
```
用途: 获取公开成果详情
权限: 公开
路径参数:
```json
{ "id": {"type": "number", "required": true} }
```

### 3. 公开用户列表
```
GET /open/users
```
用途: 对外展示公开用户信息
权限: 公开
查询参数:
```json
{
  "pageNum": {"type": "number", "default": 1},
  "pageSize": {"type": "number", "default": 1000},
  "identity": {"type": "number", "remark": "1管理员 2教师 3学生"},
  "academicStatus": {"type": "number", "remark": "1..5"},
  "keyword": {"type": "string"}
}
```

### 4. 公开用户详情
```
GET /open/users/{id}
```
用途: 获取公开用户详情
权限: 公开
路径参数:
```json
{ "id": {"type": "number", "required": true} }
```

### 5. 某用户的公开成果
```
GET /open/users/{userId}/achievements
```
用途: 获取某用户的公开成果列表
权限: 公开
路径/查询参数:
```json
{ "path": {"userId": {"type": "number", "required": true}}, "query": {"pageNum": {"type": "number", "default": 1}, "pageSize": {"type": "number", "default": 10}} }
```
