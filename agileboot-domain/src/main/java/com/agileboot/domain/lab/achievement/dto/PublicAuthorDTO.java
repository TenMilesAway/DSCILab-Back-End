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

    @Schema(description = "作者记录ID")
    private Long id;

    @Schema(description = "内部作者用户ID（外部作者为null）")
    private Long userId;

    @Schema(description = "作者姓名")
    private String name;

    @Schema(description = "英文姓名")
    private String nameEn;

    @Schema(description = "邮箱（用于确认作者身份）")
    private String email;

    @Schema(description = "单位/机构")
    private String affiliation;

    @Schema(description = "作者顺序")
    private Integer authorOrder;

    @Schema(description = "是否通讯作者")
    private Boolean isCorresponding;

    @Schema(description = "作者角色/贡献")
    private String role;

    @Schema(description = "是否在个人页可见（仅内部作者有效）")
    private Boolean visible;

    /**
     * 从作者实体转换为公开DTO
     */
    public static PublicAuthorDTO fromEntity(LabAchievementAuthorEntity entity) {
        if (entity == null) {
            return null;
        }

        PublicAuthorDTO dto = new PublicAuthorDTO();
        dto.setId(entity.getId());
        dto.setUserId(entity.getUserId());
        dto.setName(entity.getName());
        dto.setNameEn(entity.getNameEn());
        dto.setEmail(entity.getEmail());
        dto.setAffiliation(entity.getAffiliation());
        dto.setAuthorOrder(entity.getAuthorOrder());
        dto.setIsCorresponding(entity.getIsCorresponding());
        dto.setRole(entity.getRole());
        dto.setVisible(entity.getVisible());

        return dto;
    }
}
