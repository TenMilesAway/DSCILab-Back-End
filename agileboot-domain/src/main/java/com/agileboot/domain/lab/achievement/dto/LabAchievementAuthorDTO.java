package com.agileboot.domain.lab.achievement.dto;

import com.agileboot.domain.lab.achievement.db.LabAchievementAuthorEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.Date;

/**
 * 成果作者 DTO（后台使用）
 */
@Data
@Schema(description = "成果作者")
public class LabAchievementAuthorDTO {

    @Schema(description = "作者ID")
    private Long id;

    @Schema(description = "成果ID")
    private Long achievementId;

    @Schema(description = "用户ID（内部作者）")
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

    @Schema(description = "是否在个人页可见")
    private Boolean visible;

    @Schema(description = "是否内部作者")
    private Boolean isInternal;

    @Schema(description = "创建时间")
    private Date createTime;

    @Schema(description = "更新时间")
    private Date updateTime;

    /**
     * 从实体转换
     */
    public static LabAchievementAuthorDTO fromEntity(LabAchievementAuthorEntity entity) {
        if (entity == null) {
            return null;
        }

        LabAchievementAuthorDTO dto = new LabAchievementAuthorDTO();
        dto.setId(entity.getId());
        dto.setAchievementId(entity.getAchievementId());
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
