package com.agileboot.admin.controller.lab;

import com.agileboot.common.core.base.BaseController;
import com.agileboot.common.core.dto.ResponseDTO;
import com.agileboot.domain.lab.user.db.LabUserEntity;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 实验室相关字典接口
 */
@Tag(name = "实验室字典接口")
@RestController
@RequestMapping("/lab/dicts")
public class LabDictController extends BaseController {

    @Data
    @AllArgsConstructor
    public static class DictItem {
        private Integer value;
        private String label;
    }

    @Operation(summary = "学术身份字典", description = "返回学术身份可选项：0=实验室负责人,1=教授,2=副教授,3=讲师,4=博士,5=硕士,6=本科")
    @GetMapping("/academic-status")
    public ResponseDTO<List<DictItem>> academicStatusDict() {
        List<DictItem> items = Arrays.stream(LabUserEntity.AcademicStatus.values())
            .map(e -> new DictItem(e.getCode(), e.getDesc()))
            .collect(Collectors.toList());
        return ResponseDTO.ok(items);
    }
}

