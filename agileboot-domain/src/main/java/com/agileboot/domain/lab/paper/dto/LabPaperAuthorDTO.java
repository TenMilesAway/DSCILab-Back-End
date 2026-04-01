package com.agileboot.domain.lab.paper.dto;

import com.agileboot.domain.lab.paper.author.LabPaperAuthorEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.Date;

@Data
@Schema(description = "论文作者")
public class LabPaperAuthorDTO {

    @Schema(description = "作者ID")
    private Long id;

    @Schema(description = "论文ID")
    private Long paperId;

    @Schema(description = "用户ID（内部作者）")
    private Long userId;

    @Schema(description = "作者姓名")
    private String name;

    @Schema(description = "英文姓名")
    private String nameEn;

    @Schema(description = "邮箱")
    private String email;

    @Schema(description = "单位/机构")
    private String affiliation;

    @Schema(description = "作者顺序")
    private Integer authorOrder;

    @Schema(description = "是否通讯作者")
    private Boolean isCorresponding;

    @Schema(description = "作者角色/贡献")
    private String role;

    @Schema(description = "是否在个人页可见")
    private Boolean visible;

    @Schema(description = "是否内部作者")
    private Boolean isInternal;

    @Schema(description = "创建时间")
    private Date createTime;

    @Schema(description = "更新时间")
    private Date updateTime;

    public static LabPaperAuthorDTO fromEntity(LabPaperAuthorEntity entity) {
        if (entity == null) {
            return null;
        }
        LabPaperAuthorDTO dto = new LabPaperAuthorDTO();
        dto.setId(entity.getId());
        dto.setPaperId(entity.getPaperId());
        dto.setUserId(entity.getUserId());
        dto.setName(entity.getName());
        dto.setNameEn(entity.getNameEn());
        dto.setEmail(entity.getEmail());
        dto.setAffiliation(entity.getAffiliation());
        dto.setAuthorOrder(entity.getAuthorOrder());
        dto.setIsCorresponding(entity.getIsCorresponding());
        dto.setRole(entity.getRole());
        dto.setVisible(entity.getVisible());
        dto.setIsInternal(entity.isInternal());
        dto.setCreateTime(entity.getCreateTime());
        dto.setUpdateTime(entity.getUpdateTime());
        return dto;
    }
}
