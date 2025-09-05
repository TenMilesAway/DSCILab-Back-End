package com.agileboot.domain.lab.achievement.dto;

import com.agileboot.domain.lab.achievement.db.LabAchievementAuthorEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 公开作者DTO（精简版）
 */
@Data
@Schema(description = "公开作者DTO")
public class PublicAuthorDTO {

    @Schema(description = "作者姓名")
    private String name;

    @Schema(description = "英文姓名")
    private String nameEn;

    @Schema(description = "单位/机构")
    private String affiliation;

    @Schema(description = "作者顺序")
    private Integer authorOrder;

    @Schema(description = "是否通讯作者")
    private Boolean isCorresponding;

    @Schema(description = "作者角色/贡献")
    private String role;

    /**
     * 从作者实体转换为公开DTO
     */
    public static PublicAuthorDTO fromEntity(LabAchievementAuthorEntity entity) {
        if (entity == null) {
            return null;
        }
        
        PublicAuthorDTO dto = new PublicAuthorDTO();
        dto.setName(entity.getName());
        dto.setNameEn(entity.getNameEn());
        dto.setAffiliation(entity.getAffiliation());
        dto.setAuthorOrder(entity.getAuthorOrder());
        dto.setIsCorresponding(entity.getIsCorresponding());
        dto.setRole(entity.getRole());
        
        return dto;
    }
}
