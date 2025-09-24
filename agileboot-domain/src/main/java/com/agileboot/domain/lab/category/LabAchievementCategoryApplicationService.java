package com.agileboot.domain.lab.category;

import com.agileboot.common.core.page.PageDTO;
import com.agileboot.common.exception.ApiException;
import com.agileboot.common.exception.error.ErrorCode;
import com.agileboot.domain.lab.category.command.BatchUpdateSortCommand;
import com.agileboot.domain.lab.category.command.CreateCategoryCommand;
import com.agileboot.domain.lab.category.command.UpdateCategoryCommand;
import com.agileboot.domain.lab.category.db.LabAchievementCategoryEntity;
import com.agileboot.domain.lab.category.db.LabAchievementCategoryService;
import com.agileboot.domain.lab.category.dto.LabAchievementCategoryDTO;
import com.agileboot.domain.lab.category.query.CategoryQuery;
import com.agileboot.infrastructure.user.AuthenticationUtils;
import com.agileboot.infrastructure.user.web.SystemLoginUser;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 实验室成果类型应用服务
 *
 * @author agileboot
 */
@Service
@RequiredArgsConstructor
public class LabAchievementCategoryApplicationService {

    private final LabAchievementCategoryService categoryService;

    /**
     * 获取类型树形结构
     */
    public List<LabAchievementCategoryDTO> getCategoryTree(boolean includeInactive) {
        List<LabAchievementCategoryEntity> categories = categoryService.getCategoryTree(includeInactive);
        return categories.stream()
                .map(LabAchievementCategoryDTO::new)
                .collect(Collectors.toList());
    }

    /**
     * 分页查询类型列表
     */
    public PageDTO<LabAchievementCategoryDTO> getCategoryList(CategoryQuery query) {
        LambdaQueryWrapper<LabAchievementCategoryEntity> wrapper = new LambdaQueryWrapper<>();

        // 关键词搜索
        if (StringUtils.hasText(query.getKeyword())) {
            wrapper.and(w -> w.like(LabAchievementCategoryEntity::getCategoryName, query.getKeyword())
                    .or().like(LabAchievementCategoryEntity::getCategoryCode, query.getKeyword())
                    .or().like(LabAchievementCategoryEntity::getCategoryNameEn, query.getKeyword()));
        }

        // 父类型过滤
        if (query.getParentId() != null) {
            wrapper.eq(LabAchievementCategoryEntity::getParentId, query.getParentId());
        }

        // 只查询一级类型
        if (Boolean.TRUE.equals(query.getTopLevelOnly())) {
            wrapper.isNull(LabAchievementCategoryEntity::getParentId);
        }

        // 只查询二级类型
        if (Boolean.TRUE.equals(query.getSecondLevelOnly())) {
            wrapper.isNotNull(LabAchievementCategoryEntity::getParentId);
        }

        // 启用状态过滤
        if (query.getIsActive() != null) {
            wrapper.eq(LabAchievementCategoryEntity::getIsActive, query.getIsActive());
        }

        // 系统内置过滤
        if (query.getIsSystem() != null) {
            wrapper.eq(LabAchievementCategoryEntity::getIsSystem, query.getIsSystem());
        }

        // 类型编码过滤
        if (StringUtils.hasText(query.getCategoryCode())) {
            wrapper.eq(LabAchievementCategoryEntity::getCategoryCode, query.getCategoryCode());
        }

        // 排序
        wrapper.orderByAsc(LabAchievementCategoryEntity::getSortOrder);

        IPage<LabAchievementCategoryEntity> page = new Page<>(query.getPageNum(), query.getPageSize());
        IPage<LabAchievementCategoryEntity> result = categoryService.page(page, wrapper);

        List<LabAchievementCategoryDTO> dtoList = result.getRecords().stream()
                .map(entity -> {
                    LabAchievementCategoryDTO dto = new LabAchievementCategoryDTO(entity);
                    // 设置额外信息
                    dto.setHasChildren(categoryService.hasChildren(entity.getId()));
                    dto.setIsUsed(categoryService.isUsedByAchievements(entity.getId()));
                    return dto;
                })
                .collect(Collectors.toList());

        return new PageDTO<>(dtoList, result.getTotal());
    }

    /**
     * 获取类型详情
     */
    public LabAchievementCategoryDTO getCategoryById(Long id) {
        LabAchievementCategoryEntity entity = categoryService.getById(id);
        if (entity == null) {
            throw new ApiException(ErrorCode.Business.COMMON_OBJECT_NOT_FOUND, "类型不存在");
        }

        LabAchievementCategoryDTO dto = new LabAchievementCategoryDTO(entity);
        dto.setHasChildren(categoryService.hasChildren(id));
        dto.setIsUsed(categoryService.isUsedByAchievements(id));

        return dto;
    }

    /**
     * 创建类型
     */
    @Transactional(rollbackFor = Exception.class)
    public Long createCategory(CreateCategoryCommand command) {
        // 验证业务规则
        validateCreateCommand(command);

        // 若未提供类型编码，按规则自动生成唯一编码
        String code = org.springframework.util.StringUtils.hasText(command.getCategoryCode())
            ? command.getCategoryCode()
            : generateUniqueCategoryCode(command.getParentId(), command.getCategoryName());

        // 创建实体
        LabAchievementCategoryEntity entity = new LabAchievementCategoryEntity();
        entity.setParentId(command.getParentId());
        entity.setCategoryCode(code);
        entity.setCategoryName(command.getCategoryName());
        entity.setCategoryNameEn(command.getCategoryNameEn());
        entity.setDescription(command.getDescription());
        entity.setIsSystem(false); // 用户创建的都不是系统内置类型
        entity.setIsActive(command.getIsActive() != null ? command.getIsActive() : true);
        entity.setIcon(command.getIcon());
        entity.setColor(command.getColor());

        // 设置排序号
        if (command.getSortOrder() != null) {
            entity.setSortOrder(command.getSortOrder());
        } else {
            entity.setSortOrder(categoryService.getNextSortOrder(command.getParentId()));
        }

        // 设置审计信息
        SystemLoginUser loginUser = AuthenticationUtils.getSystemLoginUser();
        if (loginUser != null) {
            entity.setCreatorId(loginUser.getUserId());
        }
        entity.setCreateTime(new Date());
        entity.setUpdateTime(new Date());
        entity.setDeleted(false);

        categoryService.save(entity);
        return entity.getId();
    }

    /**
     * 更新类型
     */
    @Transactional(rollbackFor = Exception.class)
    public void updateCategory(Long id, UpdateCategoryCommand command) {
        LabAchievementCategoryEntity entity = categoryService.getById(id);
        if (entity == null) {
            throw new ApiException(ErrorCode.Business.COMMON_OBJECT_NOT_FOUND, "类型不存在");
        }

        // 验证业务规则
        validateUpdateCommand(id, command);

        // 更新字段
        if (command.getParentId() != null || command.getParentId() == null) {
            entity.setParentId(command.getParentId());
        }
        if (StringUtils.hasText(command.getCategoryCode())) {
            entity.setCategoryCode(command.getCategoryCode());
        }
        if (StringUtils.hasText(command.getCategoryName())) {
            entity.setCategoryName(command.getCategoryName());
        }
        if (command.getCategoryNameEn() != null) {
            entity.setCategoryNameEn(command.getCategoryNameEn());
        }
        if (command.getDescription() != null) {
            entity.setDescription(command.getDescription());
        }
        if (command.getSortOrder() != null) {
            entity.setSortOrder(command.getSortOrder());
        }
        if (command.getIsActive() != null) {
            entity.setIsActive(command.getIsActive());
        }
        if (command.getIcon() != null) {
            entity.setIcon(command.getIcon());
        }
        if (command.getColor() != null) {
            entity.setColor(command.getColor());
        }

        // 设置审计信息
        SystemLoginUser loginUser = AuthenticationUtils.getSystemLoginUser();
        if (loginUser != null) {
            entity.setUpdaterId(loginUser.getUserId());
        }
        entity.setUpdateTime(new Date());

        categoryService.updateById(entity);
    }

    /**
     * 删除类型
     */
    @Transactional(rollbackFor = Exception.class)
    public void deleteCategory(Long id) {
        LabAchievementCategoryEntity entity = categoryService.getById(id);
        if (entity == null) {
            throw new ApiException(ErrorCode.Business.COMMON_OBJECT_NOT_FOUND, "类型不存在");
        }

        // 精确提示不可删除的原因
        if (Boolean.TRUE.equals(entity.getIsSystem())) {
            throw new ApiException(ErrorCode.Business.COMMON_UNSUPPORTED_OPERATION);
        }
        if (categoryService.isUsedByAchievements(id)) {
            // 需求：提示“该类型存在成果，不允许删除”
            throw new ApiException(ErrorCode.Business.COMMON_UNSUPPORTED_OPERATION, "该类型存在成果，不允许删除");
        }
        if (categoryService.hasChildren(id)) {
            // 类型存在下级时的专用提示
            throw new ApiException(ErrorCode.Business.COMMON_UNSUPPORTED_OPERATION, "该类型存在下级类型，不允许删除");
        }

        // 修改 category_code 以避免唯一约束冲突
        String originalCode = entity.getCategoryCode();
        String deletedCode = originalCode + "_deleted_" + System.currentTimeMillis();
        entity.setCategoryCode(deletedCode);
        categoryService.updateById(entity);

        // 然后执行软删除
        categoryService.removeById(id);
    }

    /**
     * 强制删除"未分类"系统类型
     */
    @Transactional(rollbackFor = Exception.class)
    public void forceDeleteUncategorizedType() {
        // 查找"未分类"类型
        LabAchievementCategoryEntity uncategorized = categoryService.getByCategoryCode("UNCATEGORIZED");
        if (uncategorized == null) {
            throw new ApiException(ErrorCode.Business.COMMON_OBJECT_NOT_FOUND, "未分类类型不存在");
        }

        // 检查是否被成果使用
        if (categoryService.isUsedByAchievements(uncategorized.getId())) {
            throw new ApiException(ErrorCode.Business.COMMON_UNSUPPORTED_OPERATION, "该类型存在成果，不允许删除");
        }

        // 检查是否有子类型
        if (categoryService.hasChildren(uncategorized.getId())) {
            throw new ApiException(ErrorCode.Business.COMMON_UNSUPPORTED_OPERATION, "该类型存在下级类型，不允许删除");
        }

        // 修改 category_code 以避免唯一约束冲突
        String deletedCode = "UNCATEGORIZED_deleted_" + System.currentTimeMillis();
        uncategorized.setCategoryCode(deletedCode);
        categoryService.updateById(uncategorized);

        // 执行软删除
        categoryService.removeById(uncategorized.getId());
    }

    /**
     * 批量更新排序
     */
    @Transactional(rollbackFor = Exception.class)
    public void batchUpdateSort(BatchUpdateSortCommand command) {
        List<LabAchievementCategoryEntity> categories = command.getItems().stream()
                .map(item -> {
                    LabAchievementCategoryEntity entity = new LabAchievementCategoryEntity();
                    entity.setId(item.getId());
                    entity.setSortOrder(item.getSortOrder());
                    return entity;
                })
                .collect(Collectors.toList());

        categoryService.batchUpdateSortOrder(categories);
    }

    /**
     * 启用/禁用类型
     */
    @Transactional(rollbackFor = Exception.class)
    public void updateCategoryStatus(Long id, boolean active) {
        LabAchievementCategoryEntity entity = categoryService.getById(id);
        if (entity == null) {
            throw new ApiException(ErrorCode.Business.COMMON_OBJECT_NOT_FOUND, "类型不存在");
        }

        entity.setIsActive(active);
        entity.setUpdateTime(new Date());

        SystemLoginUser loginUser = AuthenticationUtils.getSystemLoginUser();
        if (loginUser != null) {
            entity.setUpdaterId(loginUser.getUserId());
        }

        categoryService.updateById(entity);
    }

    /**
     * 验证创建命令
     */
    private void validateCreateCommand(CreateCategoryCommand command) {
        // 检查类型编码是否重复（仅当提供了自定义编码时）
        if (org.springframework.util.StringUtils.hasText(command.getCategoryCode()) &&
            categoryService.isCategoryCodeDuplicated(command.getCategoryCode(), null)) {
            throw new ApiException(ErrorCode.Business.USER_NAME_IS_NOT_UNIQUE, "类型编码已存在");
        }

        // 检查同一父类型下名称是否重复
        if (categoryService.isCategoryNameDuplicated(command.getParentId(), command.getCategoryName(), null)) {
            throw new ApiException(ErrorCode.Business.USER_NAME_IS_NOT_UNIQUE, "同一父类型下类型名称已存在");
        }

        // 检查父类型是否存在
        if (command.getParentId() != null) {
            LabAchievementCategoryEntity parent = categoryService.getById(command.getParentId());
            if (parent == null) {
                throw new ApiException(ErrorCode.Business.COMMON_OBJECT_NOT_FOUND, "父类型不存在");
            }

            // 检查层级深度（最多2级）
            if (parent.getParentId() != null) {
                throw new ApiException(ErrorCode.Client.COMMON_REQUEST_PARAMETERS_INVALID, "最多支持2级分类");
            }
        }
    }

    /**
     * 验证更新命令
     */
    private void validateUpdateCommand(Long id, UpdateCategoryCommand command) {
        // 检查类型编码是否重复
        if (StringUtils.hasText(command.getCategoryCode()) &&
            categoryService.isCategoryCodeDuplicated(command.getCategoryCode(), id)) {
            throw new ApiException(ErrorCode.Business.USER_NAME_IS_NOT_UNIQUE, "类型编码已存在");
        }

        // 检查同一父类型下名称是否重复
        if (StringUtils.hasText(command.getCategoryName()) &&
            categoryService.isCategoryNameDuplicated(command.getParentId(), command.getCategoryName(), id)) {
            throw new ApiException(ErrorCode.Business.USER_NAME_IS_NOT_UNIQUE, "同一父类型下类型名称已存在");
        }

        // 检查是否会造成循环引用
        if (command.getParentId() != null &&
            categoryService.wouldCauseCyclicReference(id, command.getParentId())) {
            throw new ApiException(ErrorCode.Client.COMMON_REQUEST_PARAMETERS_INVALID, "不能将类型移动到自己的子类型下");
        }

        // 检查父类型是否存在
        if (command.getParentId() != null) {
            LabAchievementCategoryEntity parent = categoryService.getById(command.getParentId());
            if (parent == null) {
                throw new ApiException(ErrorCode.Business.COMMON_OBJECT_NOT_FOUND, "父类型不存在");
            }

            // 检查层级深度（最多2级）
            if (parent.getParentId() != null) {
                throw new ApiException(ErrorCode.Client.COMMON_REQUEST_PARAMETERS_INVALID, "最多支持2级分类");
            }
        }
    }

    /**
     * 生成唯一的 category_code：
     * 规则：PARENT_CODE(可选)+"_"+名称转大写下划线，超长截断，若冲突则追加递增后缀。
     */
    private String generateUniqueCategoryCode(Long parentId, String categoryName) {
        String parentCode = null;
        if (parentId != null) {
            LabAchievementCategoryEntity parent = categoryService.getById(parentId);
            if (parent != null && org.springframework.util.StringUtils.hasText(parent.getCategoryCode())) {
                parentCode = parent.getCategoryCode().toUpperCase();
            }
        }
        String base = normalizeToCode(categoryName);
        if (!org.springframework.util.StringUtils.hasText(base)) {
            base = "CAT";
        }
        final int maxLen = 50;
        String prefix = org.springframework.util.StringUtils.hasText(parentCode) ? parentCode + "_" : "";
        int available = Math.max(1, maxLen - prefix.length());
        if (base.length() > available) {
            base = base.substring(0, available);
        }
        String candidate = prefix + base;
        int i = 0;
        while (categoryService.isCategoryCodeDuplicated(candidate, null) && i < 1000) {
            i++;
            String suffix = "_" + i;
            int avail = Math.max(1, maxLen - prefix.length() - suffix.length());
            String head = base.length() > avail ? base.substring(0, avail) : base;
            candidate = prefix + head + suffix;
        }
        return candidate;
    }

    /** 将任意名称转为大写下划线CODE：非字母数字替换为 '_'，合并多余下划线，去首尾下划线 */
    private String normalizeToCode(String s) {
        if (s == null) return null;
        String code = s.trim().toUpperCase();
        // 非字母数字转下划线
        code = code.replaceAll("[^A-Z0-9]+", "_");
        // 合并多余下划线并去首尾
        code = code.replaceAll("_+", "_");
        if (code.startsWith("_")) code = code.substring(1);
        if (code.endsWith("_")) code = code.substring(0, code.length() - 1);
        return code;
    }
}
