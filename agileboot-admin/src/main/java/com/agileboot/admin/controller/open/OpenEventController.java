package com.agileboot.admin.controller.open;

import com.agileboot.common.core.base.BaseController;
import com.agileboot.common.core.dto.ResponseDTO;
import com.agileboot.common.core.page.PageDTO;
import com.agileboot.common.exception.ApiException;
import com.agileboot.common.exception.error.ErrorCode;
import com.agileboot.domain.lab.event.LabEventApplicationService;
import com.agileboot.domain.lab.event.dto.LabEventDTO;
import com.agileboot.domain.lab.event.dto.LabEventListDTO;
import com.agileboot.domain.lab.event.dto.PublicEventDetailDTO;
import com.agileboot.domain.lab.event.dto.PublicEventListDTO;
import com.agileboot.domain.lab.event.query.PublicEventQuery;
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

import java.util.List;
import java.util.stream.Collectors;

@Tag(name = "公开活动", description = "公开活动展示接口")
@RestController
@RequestMapping("/open/events")
@RequiredArgsConstructor
public class OpenEventController extends BaseController {

    private final LabEventApplicationService eventApplicationService;

    @Operation(summary = "公开活动列表")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "查询成功",
            content = @Content(schema = @Schema(implementation = PageDTO.class)))
    })
    @GetMapping
    public ResponseDTO<PageDTO<PublicEventListDTO>> list(@Parameter(description = "查询条件") PublicEventQuery query) {
        PageDTO<LabEventListDTO> page = eventApplicationService.getPublicEventList(query);
        List<PublicEventListDTO> list = page.getRows().stream()
            .map(PublicEventListDTO::fromLabList)
            .collect(Collectors.toList());
        return ResponseDTO.ok(new PageDTO<>(list, page.getTotal()));
    }

    @Operation(summary = "公开活动详情")
    @GetMapping("/{id}")
    public ResponseDTO<PublicEventDetailDTO> detail(@PathVariable Long id) {
        LabEventDTO dto = eventApplicationService.getEventDetail(id);
        if (dto == null || Boolean.FALSE.equals(dto.getPublished())) {
            throw new ApiException(ErrorCode.Business.COMMON_OBJECT_NOT_FOUND, "", "活动");
        }
        return ResponseDTO.ok(PublicEventDetailDTO.fromLabDetail(dto));
    }
}
