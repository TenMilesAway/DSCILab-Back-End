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

    @Operation(summary = "成果类型字典", description = "返回成果类型：1=论文,2=项目")
    @GetMapping("/achievement-types")
    public ResponseDTO<List<DictItem>> achievementTypesDict() {
        List<DictItem> items = Arrays.asList(
            new DictItem(1, "论文"),
            new DictItem(2, "项目")
        );
        return ResponseDTO.ok(items);
    }

    @Operation(summary = "论文类型字典", description = "返回论文子类型：1=期刊论文,2=会议论文,3=书籍章节,4=专利,5=标准,6=研究报告,7=其他论文")
    @GetMapping("/paper-types")
    public ResponseDTO<List<DictItem>> paperTypesDict() {
        List<DictItem> items = Arrays.asList(
            new DictItem(1, "期刊论文"),
            new DictItem(2, "会议论文"),
            new DictItem(3, "书籍章节"),
            new DictItem(4, "专利"),
            new DictItem(5, "标准"),
            new DictItem(6, "研究报告"),
            new DictItem(7, "其他论文")
        );
        return ResponseDTO.ok(items);
    }

    @Operation(summary = "项目类型字典", description = "返回项目子类型：1=国家重点项目,2=国家一般项目,3=省部级项目,4=企业合作项目,5=国际合作项目,6=青年基金,7=博士后基金,8=其他项目")
    @GetMapping("/project-types")
    public ResponseDTO<List<DictItem>> projectTypesDict() {
        List<DictItem> items = Arrays.asList(
            new DictItem(1, "国家重点项目"),
            new DictItem(2, "国家一般项目"),
            new DictItem(3, "省部级项目"),
            new DictItem(4, "企业合作项目"),
            new DictItem(5, "国际合作项目"),
            new DictItem(6, "青年基金"),
            new DictItem(7, "博士后基金"),
            new DictItem(8, "其他项目")
        );
        return ResponseDTO.ok(items);
    }
}

