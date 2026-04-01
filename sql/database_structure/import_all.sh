#!/bin/bash

# 数据库导入脚本
# 使用方法: ./import_all.sh [数据库名] [用户名]

DB_NAME=${1:-web}
DB_USER=${2:-root}

echo "开始导入数据库结构到 $DB_NAME..."
echo "使用用户: $DB_USER"
echo "================================"

# 检查MySQL连接
if ! mysql -u $DB_USER -p -e "USE $DB_NAME;" 2>/dev/null; then
    echo "错误: 无法连接到数据库 $DB_NAME"
    exit 1
fi

# 按顺序导入SQL文件
echo "1. 导入系统基础表..."
mysql -u $DB_USER -p $DB_NAME < sys_config.sql
mysql -u $DB_USER -p $DB_NAME < sys_dept.sql
mysql -u $DB_USER -p $DB_NAME < sys_menu.sql
mysql -u $DB_USER -p $DB_NAME < sys_post.sql
mysql -u $DB_USER -p $DB_NAME < sys_role.sql
mysql -u $DB_USER -p $DB_NAME < sys_user.sql

echo "2. 导入系统关联表..."
mysql -u $DB_USER -p $DB_NAME < sys_role_menu.sql
mysql -u $DB_USER -p $DB_NAME < sys_user_type_config.sql

echo "3. 导入系统日志表..."
mysql -u $DB_USER -p $DB_NAME < sys_login_info.sql
mysql -u $DB_USER -p $DB_NAME < sys_notice.sql
mysql -u $DB_USER -p $DB_NAME < sys_operation_log.sql

echo "4. 导入实验室核心表..."
mysql -u $DB_USER -p $DB_NAME < lab_user.sql
mysql -u $DB_USER -p $DB_NAME < lab_achievement_category.sql
mysql -u $DB_USER -p $DB_NAME < lab_achievement.sql
mysql -u $DB_USER -p $DB_NAME < lab_achievement_author.sql

echo "5. 导入实验室业务表..."
mysql -u $DB_USER -p $DB_NAME < lab_member.sql
mysql -u $DB_USER -p $DB_NAME < lab_paper.sql
mysql -u $DB_USER -p $DB_NAME < lab_fund.sql
mysql -u $DB_USER -p $DB_NAME < lab_fundamount.sql
mysql -u $DB_USER -p $DB_NAME < lab_activity.sql
mysql -u $DB_USER -p $DB_NAME < lab_type.sql
mysql -u $DB_USER -p $DB_NAME < lab_user_relationship.sql

echo "6. 导入关系表..."
mysql -u $DB_USER -p $DB_NAME < rs_fund_paper.sql
mysql -u $DB_USER -p $DB_NAME < rs_member_fund.sql
mysql -u $DB_USER -p $DB_NAME < rs_member_paper.sql

echo "================================"
echo "数据库导入完成！"
echo "验证导入结果..."

# 验证导入结果
mysql -u $DB_USER -p $DB_NAME -e "
SHOW TABLES;
SELECT '实验室表数量:' AS info, COUNT(*) AS count FROM information_schema.tables WHERE table_schema='$DB_NAME' AND table_name LIKE 'lab_%';
SELECT '系统表数量:' AS info, COUNT(*) AS count FROM information_schema.tables WHERE table_schema='$DB_NAME' AND table_name LIKE 'sys_%';
"
