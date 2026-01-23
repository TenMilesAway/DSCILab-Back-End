package com.agileboot.admin.controller.open;

import com.agileboot.common.core.base.BaseController;
import com.agileboot.common.core.dto.ResponseDTO;
import com.agileboot.common.core.page.PageDTO;
import com.agileboot.common.exception.ApiException;
import com.agileboot.common.exception.error.ErrorCode;
import com.agileboot.domain.lab.project.LabProjectApplicationService;
import com.agileboot.domain.lab.project.dto.LabProjectDTO;
import com.agileboot.domain.lab.project.query.LabProjectQuery;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "公开项目", description = "公开项目展示接口")
@RestController
@RequestMapping("/open/projects")
@RequiredArgsConstructor
public class OpenProjectController extends BaseController {

    private final LabProjectApplicationService projectApplicationService;

    @Operation(summary = "公开项目列表")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "查询成功",
            content = @Content(schema = @Schema(implementation = PageDTO.class)))
    })
    @GetMapping
    public ResponseDTO<PageDTO<LabProjectDTO>> list(
        @Parameter(description = "查询条件") LabProjectQuery query) {
        PageDTO<LabProjectDTO> pageDTO = projectApplicationService.getPublicProjectList(query);
        return ResponseDTO.ok(pageDTO);
    }

    @Operation(summary = "公开项目详情")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "查询成功",
            content = @Content(schema = @Schema(implementation = LabProjectDTO.class))),
        @ApiResponse(responseCode = "404", description = "项目不存在")
    })
    @GetMapping("/{id}")
    public ResponseDTO<LabProjectDTO> detail(@PathVariable Long id) {
        LabProjectDTO dto = projectApplicationService.getProjectDetail(id);
        if (dto == null || Boolean.FALSE.equals(dto.getPublished())) {
            throw new ApiException(ErrorCode.Business.COMMON_OBJECT_NOT_FOUND, "", "项目");
        }
        return ResponseDTO.ok(dto);
    }
}
