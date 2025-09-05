## Lab Backend API Spec (Users, Achievements, Open)

- Base URL: http://localhost:8080
- Auth: Bearer JWT from /login
- Response wrapper: { code, msg, data }
- 注意：字段清单均为 JSON 展示（包含 type/required/remark），接口摘要与分组采用 Markdown

---

### Auth

- POST /login
  - Body 字段
    {
      "username": {"type": "string", "required": true, "remark": "lab_user.username"},
      "password": {"type": "string", "required": true},
      "code": {"type": "string", "required": false},
      "uuid": {"type": "string", "required": false}
    }
  - 成功响应 data 结构
    {
      "token": {"type": "string", "remark": "JWT"},
      "currentUser": {
        "userInfo": {
          "id": {"type": "number"},
          "username": {"type": "string"},
          "realName": {"type": "string"},
          "gender": {"type": "number", "remark": "0未知 1男 2女"},
          "genderDesc": {"type": "string"},
          "identity": {"type": "number", "remark": "1管理员 2教师 3学生"},
          "email": {"type": "string"},
          "phone": {"type": "string"},
          "photo": {"type": "string"},
          "isActive": {"type": "boolean"},
          "createTime": {"type": "string"},
          "updateTime": {"type": "string"}
        },
        "roleKey": {"type": "string", "remark": "lab:admin/teacher/student"},
        "permissions": {"type": "array", "items": "string"}
      }
    }

- GET /getLoginUserInfo
  - Header: Authorization: Bearer ${token}

---

### Lab Users - 管理端与自助端

- POST /lab/users/crud （创建用户，管理员）
  - Body 字段
    {
      "studentNumber": {"type": "string", "required": false},
      "username": {"type": "string", "required": true},
      "realName": {"type": "string", "required": true},
      "englishName": {"type": "string", "required": false},
      "password": {"type": "string", "required": true, "remark": "6-20位"},
      "gender": {"type": "number", "required": true, "remark": "0未知 1男 2女"},
      "identity": {"type": "number", "required": true, "remark": "1管理员 2教师 3学生"},
      "academicStatus": {"type": "number", "required": false, "remark": "0负责人 1教授 2副教授 3讲师 4博士 5硕士 6本科"},
      "researchArea": {"type": "string", "required": false},
      "phone": {"type": "string", "required": false},
      "email": {"type": "string", "required": false},
      "status": {"type": "number", "required": false, "remark": "1在读/在职 2毕业/离职"},
      "enrollmentYear": {"type": "number", "required": false},
      "graduationYear": {"type": "number", "required": false},
      "graduationDest": {"type": "string", "required": false},
      "resume": {"type": "string", "required": false},
      "homepageUrl": {"type": "string", "required": false},
      "orcid": {"type": "string", "required": false},
      "isActive": {"type": "boolean", "required": false, "default": true}
    }

- PUT /lab/users/crud/{userId} （更新用户，管理员）
  - Path: userId:number 必填
  - Body 字段
    {
      "studentNumber": {"type": "string", "required": false},
      "realName": {"type": "string", "required": true},
      "englishName": {"type": "string", "required": false},
      "gender": {"type": "number", "required": false},
      "identity": {"type": "number", "required": true},
      "academicStatus": {"type": "number", "required": false},
      "researchArea": {"type": "string", "required": false},
      "phone": {"type": "string", "required": false},
      "email": {"type": "string", "required": false},
      "status": {"type": "number", "required": false},
      "enrollmentYear": {"type": "number", "required": false},
      "graduationYear": {"type": "number", "required": false},
      "graduationDest": {"type": "string", "required": false},
      "resume": {"type": "string", "required": false},
      "homepageUrl": {"type": "string", "required": false},
      "orcid": {"type": "string", "required": false},
      "isActive": {"type": "boolean", "required": false}
    }

- PUT /lab/users/crud/profile （更新个人信息，自助端）
  - Body 字段
    {
      "realName": {"type": "string", "required": false},
      "englishName": {"type": "string", "required": false},
      "gender": {"type": "number", "required": false},
      "phone": {"type": "string", "required": false},
      "email": {"type": "string", "required": false},
      "researchArea": {"type": "string", "required": false},
      "homepageUrl": {"type": "string", "required": false},
      "resume": {"type": "string", "required": false}
    }

- PUT /lab/users/crud/password （修改密码，自助端）
  - Body 字段
    {
      "oldPassword": {"type": "string", "required": true},
      "newPassword": {"type": "string", "required": true}
    }

- PUT /lab/users/crud/{userId}/password （管理员重置密码）
  - Path: userId:number 必填
  - Body 字段
    {
      "password": {"type": "string", "required": true}
    }

- DELETE /lab/users/crud/{userId} （删除用户，管理员）
  - Path: userId:number 必填

- GET /lab/users/crud/list （分页列表，管理员）
  - Query 字段
    {
      "pageNum": {"type": "number", "required": false, "default": 1},
      "pageSize": {"type": "number", "required": false, "default": 10},
      "username": {"type": "string", "required": false},
      "realName": {"type": "string", "required": false},
      "englishName": {"type": "string", "required": false},
      "identity": {"type": "number", "required": false},
      "academicStatus": {"type": "number", "required": false},
      "researchAreaKeyword": {"type": "string", "required": false},
      "email": {"type": "string", "required": false},
      "phone": {"type": "string", "required": false},
      "status": {"type": "number", "required": false},
      "enrollmentYearStart": {"type": "number", "required": false},
      "enrollmentYearEnd": {"type": "number", "required": false},
      "isActive": {"type": "boolean", "required": false},
      "keyword": {"type": "string", "required": false}
    }

- GET /lab/users/crud/search （关键词搜索，管理员）
  - Query 字段
    {
      "keyword": {"type": "string", "required": true}
    }

---

### Lab Achievements - 管理端

- GET /lab/achievements （分页列表）
  - Query 字段
    {
      "pageNum": {"type": "number", "required": false, "default": 1},
      "pageSize": {"type": "number", "required": false, "default": 10},
      "keyword": {"type": "string", "required": false},
      "type": {"type": "number", "required": false, "remark": "1论文 2项目"},
      "paperType": {"type": "number", "required": false},
      "projectType": {"type": "number", "required": false},
      "published": {"type": "boolean", "required": false},
      "isVerified": {"type": "boolean", "required": false},
      "ownerUserId": {"type": "number", "required": false},
      "dateStart": {"type": "string", "required": false, "remark": "LocalDate"},
      "dateEnd": {"type": "string", "required": false, "remark": "LocalDate"}
    }

- GET /lab/achievements/{id} （详情）
  - Path: id:number 必填

- POST /lab/achievements （创建）
  - Body 字段（创建/更新共用）
    {
      "title": {"type": "string", "required": true},
      "titleEn": {"type": "string", "required": false},
      "description": {"type": "string", "required": false},
      "keywords": {"type": "string", "required": false},
      "type": {"type": "number", "required": true, "remark": "1论文 2项目"},
      "paperType": {"type": "number", "required": false},
      "projectType": {"type": "number", "required": false},
      "venue": {"type": "string", "required": false},
      "publishDate": {"type": "string", "required": false, "remark": "yyyy"},
      "projectStartDate": {"type": "string", "required": false, "remark": "yyyy-MM"},
      "projectEndDate": {"type": "string", "required": false, "remark": "yyyy-MM"},
      "coverUrl": {"type": "string", "required": false},
      "linkUrl": {"type": "string", "required": false},
      "gitUrl": {"type": "string", "required": false},
      "homepageUrl": {"type": "string", "required": false},
      "pdfUrl": {"type": "string", "required": false},
      "doi": {"type": "string", "required": false},
      "fundingAmount": {"type": "number", "required": false},
      "published": {"type": "boolean", "required": false},
      "extra": {"type": "string", "required": false},
      "authors": {"type": "array", "required": false, "items": {
        "userId": {"type": "number", "required": false},
        "name": {"type": "string", "required": false, "remark": "外部作者必填"},
        "nameEn": {"type": "string", "required": false},
        "affiliation": {"type": "string", "required": false},
        "authorOrder": {"type": "number", "required": true},
        "isCorresponding": {"type": "boolean", "required": false},
        "role": {"type": "string", "required": false},
        "visible": {"type": "boolean", "required": false}
      }}
    }

- PUT /lab/achievements/{id} （更新）
  - Path: id:number 必填
  - Body: 同上创建字段

- DELETE /lab/achievements/{id} （删除）
  - Path: id:number 必填

- PUT /lab/achievements/{id}/publish （发布/取消发布）
  - Path: id:number 必填
  - Query: { "published": {"type": "boolean", "required": true} }

- PUT /lab/achievements/{id}/verify （审核/取消审核）
  - Path: id:number 必填
  - Query: { "verified": {"type": "boolean", "required": true} }

- 成果作者管理（子资源）
  - GET /lab/achievements/{achievementId}/authors
    - Path: achievementId:number 必填
  - POST /lab/achievements/{achievementId}/authors
    - Path: achievementId:number 必填
    - Body: 见上 authors item 字段
  - PUT /lab/achievements/{achievementId}/authors/{authorId}
    - Path: achievementId:number, authorId:number 必填
    - Body: 同作者字段（均可选）
  - DELETE /lab/achievements/{achievementId}/authors/{authorId}
    - Path: achievementId:number, authorId:number 必填
  - PUT /lab/achievements/{achievementId}/authors/{authorId}/reorder
    - Query: { "newOrder": {"type": "number", "required": true} }
  - PUT /lab/achievements/{achievementId}/authors/{authorId}/visibility
    - Query: { "visible": {"type": "boolean", "required": true} }

---

### 我的成果 - 自助端

- GET /lab/my-achievements （分页列表）
  - Query 字段（与管理端类似）
    {
      "pageNum": {"type": "number", "required": false, "default": 1},
      "pageSize": {"type": "number", "required": false, "default": 10},
      "keyword": {"type": "string", "required": false},
      "type": {"type": "number", "required": false},
      "paperType": {"type": "number", "required": false},
      "projectType": {"type": "number", "required": false},
      "published": {"type": "boolean", "required": false},
      "isVerified": {"type": "boolean", "required": false},
      "dateStart": {"type": "string", "required": false},
      "dateEnd": {"type": "string", "required": false}
    }

- PUT /lab/my-achievements/{achievementId}/visibility （切换我的可见性）
  - Path: achievementId:number 必填
  - Query: { "visible": {"type": "boolean", "required": true} }

---

### 公开接口 - 无需登录

- GET /open/achievements （公开成果列表）
  - Query 字段
    {
      "pageNum": {"type": "number", "default": 1},
      "pageSize": {"type": "number", "default": 10},
      "keyword": {"type": "string", "required": false},
      "type": {"type": "number", "required": false}
    }

- GET /open/achievements/{id} （公开成果详情）
  - Path: id:number 必填

- GET /open/users （公开用户列表）
  - Query 字段
    {
      "pageNum": {"type": "number", "default": 1},
      "pageSize": {"type": "number", "default": 1000},
      "identity": {"type": "number", "required": false},
      "academicStatus": {"type": "number", "required": false},
      "keyword": {"type": "string", "required": false}
    }

- GET /open/users/{id} （公开用户详情）
  - Path: id:number 必填

