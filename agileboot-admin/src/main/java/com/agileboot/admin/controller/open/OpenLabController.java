package com.agileboot.admin.controller.open;

import com.agileboot.common.core.base.BaseController;
import com.agileboot.common.core.dto.ResponseDTO;
import com.agileboot.common.core.page.PageDTO;
import com.agileboot.domain.lab.user.db.LabUserEntity;
import com.agileboot.domain.lab.user.dto.PublicLabUserDTO;
import com.agileboot.domain.lab.user.db.LabUserService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.CacheControl;
import org.springframework.http.ResponseEntity;
import java.util.concurrent.TimeUnit;

import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 对外开放展示用接口（无需登录）。
 * 仅返回脱敏后的公开数据。
 */
@Tag(name = "开放接口 - 实验室展示", description = "对外展示用公开API（无需登录）")
@RestController
@RequestMapping("/open")
@RequiredArgsConstructor
public class OpenLabController extends BaseController {

    private final LabUserService labUserService;

    // ========= 公开用户信息 =========

    @Operation(summary = "公开用户列表", description = "分页返回公开用户信息（仅展示必要字段），只返回 is_active=1 且未删除的数据")
    @GetMapping("/users")
    public ResponseEntity<ResponseDTO<PageDTO<PublicLabUserDTO>>> listPublicUsers(
            @Parameter(description = "页码", example = "1") @RequestParam(defaultValue = "1") int pageNum,
            @Parameter(description = "每页大小", example = "10") @RequestParam(defaultValue = "10") int pageSize,
            @Parameter(description = "身份：1=管理员,2=教师,3=学生") @RequestParam(required = false) Integer identity,
            @Parameter(description = "学术身份：1..5") @RequestParam(required = false) Integer academicStatus,
            @Parameter(description = "关键词（姓名/英文名/研究方向）") @RequestParam(required = false) String keyword
    ) {
        LambdaQueryWrapper<LabUserEntity> qw = new LambdaQueryWrapper<>();
        qw.eq(LabUserEntity::getIsActive, true)
          .eq(LabUserEntity::getDeleted, false)
          .select(
              LabUserEntity::getId,
              LabUserEntity::getRealName,
              LabUserEntity::getEnglishName,
              LabUserEntity::getIdentity,
              LabUserEntity::getAcademicStatus,
              LabUserEntity::getResearchArea,
              LabUserEntity::getEnrollmentYear,
              LabUserEntity::getGraduationYear,
              LabUserEntity::getPhoto,
              LabUserEntity::getHomepageUrl,
              LabUserEntity::getEmail,
              LabUserEntity::getOrcid
          );
        if (identity != null) qw.eq(LabUserEntity::getIdentity, identity);
        if (academicStatus != null) qw.eq(LabUserEntity::getAcademicStatus, academicStatus);
        if (keyword != null && !keyword.trim().isEmpty()) {
            qw.and(w -> w.like(LabUserEntity::getRealName, keyword)
                         .or().like(LabUserEntity::getEnglishName, keyword)
                         .or().like(LabUserEntity::getResearchArea, keyword));
        }
        // 固定排序：身份(asc) -> 学术身份(asc) -> 入学年份(asc) -> id(asc)
        qw.orderByAsc(LabUserEntity::getIdentity)
          .orderByAsc(LabUserEntity::getAcademicStatus)
          .orderByAsc(LabUserEntity::getEnrollmentYear)
          .orderByAsc(LabUserEntity::getId);

        Page<LabUserEntity> page = labUserService.page(new Page<>(pageNum, pageSize), qw);
        List<PublicLabUserDTO> rows = page.getRecords().stream()
                .map(PublicLabUserDTO::fromEntity)
                .collect(Collectors.toList());
        ResponseDTO<PageDTO<PublicLabUserDTO>> body = ResponseDTO.ok(new PageDTO<>(rows, page.getTotal()));
        return ResponseEntity.ok()
                .cacheControl(CacheControl.maxAge(60, TimeUnit.SECONDS).cachePublic())
                .body(body);
    }

    @Operation(summary = "公开用户详情", description = "返回公开用户详情（仅展示必要字段）")
    @GetMapping("/users/{id}")
    public ResponseEntity<ResponseDTO<PublicLabUserDTO>> getPublicUser(@PathVariable Long id) {
        LabUserEntity e = labUserService.getById(id);
        ResponseDTO<PublicLabUserDTO> body;
        if (e == null || Boolean.TRUE.equals(e.getDeleted()) || Boolean.FALSE.equals(e.getIsActive())) {
            body = ResponseDTO.ok(null);
        } else {
            body = ResponseDTO.ok(PublicLabUserDTO.fromEntity(e));
        }
        return ResponseEntity.ok()
                .cacheControl(CacheControl.maxAge(60, TimeUnit.SECONDS).cachePublic())
                .body(body);
    }
}

