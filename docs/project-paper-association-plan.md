# 论文-项目关联功能实施计划

## 目标
实现项目与论文之间的多对多关联：
- 项目详情可查看关联论文。
- 论文详情可查看关联项目。
- 新增/编辑论文时可选择关联项目，已有论文也能补充关联。
- 可选：项目侧直接管理关联关系（视前端需求决定是否提供额外接口）。

## 实施步骤

1. **数据库设计**
   - 检查现有库是否存在项目-论文关联表；若无，新增 `lab_project_paper_rel`，字段包括：
     - `id`、`project_id`、`paper_id`、`role/remark`（可选）、审计字段、`deleted`。
     - 索引：唯一 `(project_id, paper_id, deleted)`，并对 `project_id`、`paper_id` 建普通索引；外键指向 `lab_achievement_project.id` 与 `lab_achievement_paper.id`，ON DELETE CASCADE。
   - 编写 Flyway/SQL 脚本初始化表结构。

2. **领域层开发**
   - 新建 `lab.project.relation` 包（Entity/Mapper/Service）封装项目-论文关系的 CRUD。
   - 扩展 DTO：
     - `LabProjectDTO` 增加 `List<RelatedPaperDTO>`。
     - `LabAchievementDTO` 增加 `List<RelatedProjectDTO>`。
   - `LabPaperApplicationService` 中，在创建/更新论文时处理项目关联（新增命令字段 `projectIds`）。
   - `LabProjectApplicationService` 中，查询详情/列表时批量加载关联论文；如需项目侧编辑关联，增加对应方法。

3. **接口层调整**
   - 论文管理 Controller：
     - 创建/更新接口接受 `projectIds`，调用 Service 写入关系表。
     - 详情接口返回 `relatedProjects`。
   - 项目管理 Controller：
     - 详情接口返回 `relatedPapers`。
     - 可选：新增接口用于项目侧增删关联。

4. **前端 & 文档**
   - 更新论文表单，支持多选项目。
   - 在论文详情、项目详情页展示关联列表。
   - 更新接口文档，说明新增字段与接口。

5. **测试与部署**
   - 编写单元测试：关系写入、查询回填、权限校验等。
   - 部署数据库脚本，升级后端，前端联调。
   - 如需兼容旧数据，可提供迁移脚本/工具为现有论文项目建立默认关联。

## 里程碑
1. 完成表结构与脚本。
2. 后端 domain & controller 实现、单元测试通过。
3. 前端接入并联调。
4. 回归测试与上线。
