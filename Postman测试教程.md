# 📋 Postman测试教程 - 实验室用户管理接口

## 🔧 前置准备

### 1. 安装Postman
- 下载地址: https://www.postman.com/downloads/
- 或使用Web版本: https://web.postman.co/

### 2. 基础配置
- **Base URL**: `http://localhost:8080`
- **Token**: `eyJhbGciOiJIUzUxMiJ9.eyJsb2dpbl91c2VyX2tleSI6Ijk4YTdmNmY1LTFmMjAtNDRjNy04MzliLTM0NmE1OTJjMTg5MSJ9.-Sq7yDGY5ezMCHUE-V9K3kP5d2Sl5wcMsv6NJ3FuFpBaxX8BHzSO7FwCrI1iFmVe7OcFl0wvYvJzAuOIvpb3-g`

## 🚀 创建Postman Collection

### 步骤1: 创建新的Collection
1. 打开Postman
2. 点击左侧 "Collections"
3. 点击 "+" 或 "Create Collection"
4. 命名为: `实验室用户管理API`
5. 添加描述: `DSCILab用户管理系统接口测试`

### 步骤2: 设置Collection变量
1. 在Collection上右键 → "Edit"
2. 切换到 "Variables" 标签
3. 添加以下变量:

| Variable | Initial Value | Current Value |
|----------|---------------|---------------|
| `baseUrl` | `http://localhost:8080` | `http://localhost:8080` |
| `token` | `eyJhbGciOiJIUzUxMiJ9.eyJsb2dpbl91c2VyX2tleSI6Ijk4YTdmNmY1LTFmMjAtNDRjNy04MzliLTM0NmE1OTJjMTg5MSJ9.-Sq7yDGY5ezMCHUE-V9K3kP5d2Sl5wcMsv6NJ3FuFpBaxX8BHzSO7FwCrI1iFmVe7OcFl0wvYvJzAuOIvpb3-g` | `eyJhbGciOiJIUzUxMiJ9.eyJsb2dpbl91c2VyX2tleSI6Ijk4YTdmNmY1LTFmMjAtNDRjNy04MzliLTM0NmE1OTJjMTg5MSJ9.-Sq7yDGY5ezMCHUE-V9K3kP5d2Sl5wcMsv6NJ3FuFpBaxX8BHzSO7FwCrI1iFmVe7OcFl0wvYvJzAuOIvpb3-g` |

4. 点击 "Save"

## 🧪 接口测试详细步骤

### 测试1: 健康检查接口 (无需认证)

**目的**: 验证服务是否正常运行

1. **创建请求**:
   - 点击Collection → "Add Request"
   - 命名: `健康检查`
   - 方法: `GET`
   - URL: `{{baseUrl}}/lab/users/test/health`

2. **发送请求**:
   - 点击 "Send"

3. **预期响应**:
   ```json
   {
       "code": 0,
       "msg": "操作成功",
       "data": "实验室用户服务运行正常！当前时间: 2025-08-26T15:46:45.533"
   }
   ```

4. **验证点**:
   - Status: `200 OK`
   - Response time: < 1000ms
   - `code` 字段为 `0`

---

### 测试2: 获取当前用户个人信息

**目的**: 测试JWT认证和用户信息获取

1. **创建请求**:
   - 命名: `获取当前用户信息`
   - 方法: `GET`
   - URL: `{{baseUrl}}/lab/users/profile`

2. **设置认证**:
   - 切换到 "Authorization" 标签
   - Type: `Bearer Token`
   - Token: `{{token}}`

3. **发送请求**:
   - 点击 "Send"

4. **预期响应**:
   ```json
   {
       "code": 200,
       "message": "操作成功",
       "data": {
           "id": 1,
           "studentNumber": "2021001",
           "username": "admin",
           "realName": "管理员",
           "englishName": "Admin",
           "gender": 1,
           "genderDesc": "男",
           "identity": 1,
           "identityDesc": "管理员",
           "academicStatus": null,
           "academicStatusDesc": null,
           "researchArea": "系统管理",
           "phone": "13800000001",
           "email": "admin@lab.edu",
           "status": 1,
           "statusDesc": "在读/在职",
           "enrollmentYear": 2020,
           "isActive": true,
           "createTime": "2025-08-26 15:xx:xx",
           "updateTime": "2025-08-26 15:xx:xx"
       }
   }
   ```

5. **验证点**:
   - Status: `200 OK`
   - `code` 字段为 `200`
   - `data` 包含用户详细信息

---

### 测试3: 根据用户ID获取信息

**目的**: 测试权限控制和参数传递

1. **创建请求**:
   - 命名: `根据ID获取用户信息`
   - 方法: `GET`
   - URL: `{{baseUrl}}/lab/users/1`

2. **设置认证**:
   - Authorization: `Bearer Token`
   - Token: `{{token}}`

3. **发送请求**:
   - 点击 "Send"

4. **预期响应**: 与测试2类似的用户信息

5. **额外测试**:
   - 尝试访问其他用户ID: `/lab/users/2`
   - 验证权限控制是否生效

---

### 测试4: 根据用户名获取信息

1. **创建请求**:
   - 命名: `根据用户名获取信息`
   - 方法: `GET`
   - URL: `{{baseUrl}}/lab/users/username/admin`

2. **设置认证**:
   - Authorization: `Bearer Token`
   - Token: `{{token}}`

3. **测试不同用户名**:
   - `admin` (应该成功)
   - `prof_zhang` (测试权限)
   - `nonexistent` (测试不存在的用户)

---

### 测试5: 检查用户是否存在

1. **创建请求**:
   - 命名: `检查用户ID是否存在`
   - 方法: `GET`
   - URL: `{{baseUrl}}/lab/users/1/exists`

2. **设置认证**:
   - Authorization: `Bearer Token`
   - Token: `{{token}}`

3. **预期响应**:
   ```json
   {
       "code": 200,
       "message": "操作成功",
       "data": true
   }
   ```

4. **测试用例**:
   - 存在的用户ID: `1, 2, 3, 4, 5`
   - 不存在的用户ID: `999`

---

### 测试6: 检查用户名是否存在

1. **创建请求**:
   - 命名: `检查用户名是否存在`
   - 方法: `GET`
   - URL: `{{baseUrl}}/lab/users/username/admin/exists`

2. **设置认证**:
   - Authorization: `Bearer Token`
   - Token: `{{token}}`

3. **测试用例**:
   - 存在的用户名: `admin`, `prof_zhang`, `dr_li`
   - 不存在的用户名: `nonexistent`

## 📊 批量测试和自动化

### 创建测试脚本

在每个请求的 "Tests" 标签中添加验证脚本:

```javascript
// 基础验证
pm.test("Status code is 200", function () {
    pm.response.to.have.status(200);
});

pm.test("Response time is less than 1000ms", function () {
    pm.expect(pm.response.responseTime).to.be.below(1000);
});

pm.test("Response has correct structure", function () {
    const jsonData = pm.response.json();
    pm.expect(jsonData).to.have.property('code');
    pm.expect(jsonData).to.have.property('message');
    pm.expect(jsonData).to.have.property('data');
});

// 针对成功响应的验证
pm.test("Operation successful", function () {
    const jsonData = pm.response.json();
    pm.expect(jsonData.code).to.eql(200);
});
```

### 运行Collection测试

1. 点击Collection名称旁的 "..." 
2. 选择 "Run collection"
3. 选择要运行的请求
4. 点击 "Run 实验室用户管理API"
5. 查看测试结果报告

## 🔍 常见问题和解决方案

### 问题1: 401 Unauthorized
**原因**: Token无效或过期
**解决**: 
- 检查Token是否正确设置
- 重新获取Token
- 确认Authorization header格式: `Bearer <token>`

### 问题2: 404 Not Found
**原因**: URL路径错误
**解决**:
- 检查URL拼写
- 确认服务是否启动
- 验证端口号是否正确

### 问题3: 500 Internal Server Error
**原因**: 服务器内部错误
**解决**:
- 查看服务器日志
- 检查数据库连接
- 验证请求参数格式

## 📈 高级测试技巧

### 1. 环境变量管理
创建不同环境(开发、测试、生产):
- 点击右上角环境下拉框
- 创建新环境
- 设置不同的baseUrl和token

### 2. 数据驱动测试
使用CSV文件进行批量测试:
- 创建CSV文件包含测试数据
- 在Collection Runner中导入
- 使用`{{variable}}`引用数据

### 3. 监控和报告
- 设置Collection监控
- 生成HTML测试报告
- 集成到CI/CD流程

## 🎯 测试检查清单

- [ ] 健康检查接口正常响应
- [ ] JWT认证工作正常
- [ ] 获取当前用户信息成功
- [ ] 根据ID获取用户信息成功
- [ ] 根据用户名获取信息成功
- [ ] 用户存在性检查正常
- [ ] 权限控制生效
- [ ] 错误处理正确
- [ ] 响应时间合理
- [ ] 数据格式正确

---

**提示**: 保存好你的Postman Collection，这样可以随时重复测试，也方便团队其他成员使用！
