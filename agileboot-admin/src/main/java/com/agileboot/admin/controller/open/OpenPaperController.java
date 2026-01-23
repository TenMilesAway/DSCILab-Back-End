package com.agileboot.admin.controller.open;

import com.agileboot.common.core.base.BaseController;
import com.agileboot.common.core.dto.ResponseDTO;
import com.agileboot.common.core.page.PageDTO;
import com.agileboot.common.exception.ApiException;
import com.agileboot.common.exception.error.ErrorCode;
import com.agileboot.domain.lab.paper.LabPaperApplicationService;
import com.agileboot.domain.lab.paper.dto.LabPaperDTO;
import com.agileboot.domain.lab.paper.query.PublicPaperQuery;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 公开论文接口
 */
@Tag(name = "公开论文", description = "公共论文展示接口")
@RestController
@RequestMapping("/open/papers")
@RequiredArgsConstructor
public class OpenPaperController extends BaseController {

    private final LabPaperApplicationService labPaperApplicationService;

    @Operation(summary = "公开论文列表", description = "获取公开论文列表，默认只展示已发布的论文")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "查询成功",
            content = @Content(schema = @Schema(implementation = PageDTO.class)))
    })
    @GetMapping
    public ResponseDTO<PageDTO<LabPaperDTO>> list(@Parameter(description = "查询条件") PublicPaperQuery query) {
        PageDTO<LabPaperDTO> pageDTO = labPaperApplicationService.getPublicPaperList(query);
        return ResponseDTO.ok(pageDTO);
    }

    @Operation(summary = "公开论文详情", description = "获取单个公开论文详情")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "查询成功",
            content = @Content(schema = @Schema(implementation = LabPaperDTO.class))),
        @ApiResponse(responseCode = "404", description = "论文不存在")
    })
    @GetMapping("/{id}")
    public ResponseDTO<LabPaperDTO> detail(@Parameter(description = "论文ID", required = true) @PathVariable Long id) {
        LabPaperDTO dto = labPaperApplicationService.getPaperDetail(id);
        if (dto == null || Boolean.FALSE.equals(dto.getPublished())) {
            throw new ApiException(ErrorCode.Business.COMMON_OBJECT_NOT_FOUND, "", "论文");
        }
        return ResponseDTO.ok(dto);
    }
}
