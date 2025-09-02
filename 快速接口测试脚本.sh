#!/bin/bash

# 实验室用户管理接口快速测试脚本
# 使用方法: chmod +x 快速接口测试脚本.sh && ./快速接口测试脚本.sh

# 配置
BASE_URL="http://localhost:8080"
TOKEN="eyJhbGciOiJIUzUxMiJ9.eyJsb2dpbl91c2VyX2tleSI6Ijk4YTdmNmY1LTFmMjAtNDRjNy04MzliLTM0NmE1OTJjMTg5MSJ9.-Sq7yDGY5ezMCHUE-V9K3kP5d2Sl5wcMsv6NJ3FuFpBaxX8BHzSO7FwCrI1iFmVe7OcFl0wvYvJzAuOIvpb3-g"

# 颜色定义
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# 测试函数
test_api() {
    local name="$1"
    local method="$2"
    local url="$3"
    local auth="$4"
    
    echo -e "${BLUE}🧪 测试: $name${NC}"
    echo -e "${YELLOW}URL: $method $url${NC}"
    
    if [ "$auth" = "true" ]; then
        response=$(curl -s -w "\n%{http_code}" -X "$method" "$url" \
            -H "Authorization: Bearer $TOKEN" \
            -H "Content-Type: application/json")
    else
        response=$(curl -s -w "\n%{http_code}" -X "$method" "$url" \
            -H "Content-Type: application/json")
    fi
    
    # 分离响应体和状态码
    body=$(echo "$response" | head -n -1)
    status_code=$(echo "$response" | tail -n 1)
    
    if [ "$status_code" = "200" ]; then
        echo -e "${GREEN}✅ 成功 (HTTP $status_code)${NC}"
        echo "$body" | jq '.' 2>/dev/null || echo "$body"
    else
        echo -e "${RED}❌ 失败 (HTTP $status_code)${NC}"
        echo "$body"
    fi
    
    echo -e "${YELLOW}----------------------------------------${NC}\n"
}

# 开始测试
echo -e "${GREEN}🚀 开始测试实验室用户管理接口${NC}\n"

# 测试1: 健康检查 (无需认证)
test_api "健康检查" "GET" "$BASE_URL/lab/users/test/health" "false"

# 测试2: 获取当前用户信息
test_api "获取当前用户信息" "GET" "$BASE_URL/lab/users/profile" "true"

# 测试3: 根据用户ID获取信息
test_api "根据用户ID获取信息 (ID=1)" "GET" "$BASE_URL/lab/users/1" "true"

# 测试4: 根据用户名获取信息
test_api "根据用户名获取信息 (admin)" "GET" "$BASE_URL/lab/users/username/admin" "true"

# 测试5: 检查用户ID是否存在
test_api "检查用户ID是否存在 (ID=1)" "GET" "$BASE_URL/lab/users/1/exists" "true"

# 测试6: 检查用户名是否存在
test_api "检查用户名是否存在 (admin)" "GET" "$BASE_URL/lab/users/username/admin/exists" "true"

# 测试7: 检查不存在的用户ID
test_api "检查不存在的用户ID (ID=999)" "GET" "$BASE_URL/lab/users/999/exists" "true"

# 测试8: 检查不存在的用户名
test_api "检查不存在的用户名 (nonexistent)" "GET" "$BASE_URL/lab/users/username/nonexistent/exists" "true"

# 测试其他数据库中的用户
echo -e "${BLUE}🔍 测试其他用户数据${NC}\n"

test_api "获取教授信息 (prof_zhang)" "GET" "$BASE_URL/lab/users/username/prof_zhang" "true"
test_api "获取副教授信息 (dr_li)" "GET" "$BASE_URL/lab/users/username/dr_li" "true"
test_api "获取博士生信息 (student_wang)" "GET" "$BASE_URL/lab/users/username/student_wang" "true"
test_api "获取硕士生信息 (student_liu)" "GET" "$BASE_URL/lab/users/username/student_liu" "true"

echo -e "${GREEN}🎉 测试完成！${NC}"
echo -e "${YELLOW}💡 提示: 如果看到中文乱码，这是终端显示问题，在Postman中会正常显示${NC}"
