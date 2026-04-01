package com.agileboot.domain.lab.relationship.dto;

import com.agileboot.domain.lab.relationship.db.LabUserRelationshipEntity;
import com.agileboot.domain.lab.user.dto.PublicLabUserDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDate;
import java.util.Date;

/**
 * 师生关系DTO
 *
 * @author agileboot
 */
@Data
@Schema(description = "师生关系DTO")
public class UserRelationshipDTO {

    @Schema(description = "关系ID")
    private Long id;

    @Schema(description = "导师ID")
    private Long teacherId;

    @Schema(description = "学生ID")
    private Long studentId;

    @Schema(description = "关系类型：1=导师关系")
    private Integer relationshipType;

    @Schema(description = "关系类型描述")
    private String relationshipTypeDesc;

    @Schema(description = "关系开始时间")
    private LocalDate startDate;

    @Schema(description = "关系结束时间")
    private LocalDate endDate;

    @Schema(description = "状态：1=活跃,2=已结束")
    private Integer status;

    @Schema(description = "状态描述")
    private String statusDesc;

    @Schema(description = "备注")
    private String remark;

    @Schema(description = "导师信息")
    private PublicLabUserDTO teacher;

    @Schema(description = "学生信息")
    private PublicLabUserDTO student;

    @Schema(description = "创建时间")
    private Date createTime;

    @Schema(description = "更新时间")
    private Date updateTime;

    /**
     * 从实体转换为DTO
     */
    public static UserRelationshipDTO fromEntity(LabUserRelationshipEntity entity) {
        if (entity == null) {
            return null;
        }

        UserRelationshipDTO dto = new UserRelationshipDTO();
        dto.setId(entity.getId());
        dto.setTeacherId(entity.getTeacherId());
        dto.setStudentId(entity.getStudentId());
        dto.setRelationshipType(entity.getRelationshipType());
        dto.setStartDate(entity.getStartDate());
        dto.setEndDate(entity.getEndDate());
        dto.setStatus(entity.getStatus());
        dto.setRemark(entity.getRemark());
        dto.setCreateTime(entity.getCreateTime());
        dto.setUpdateTime(entity.getUpdateTime());

        // 设置描述信息
        if (entity.getRelationshipType() != null) {
            for (LabUserRelationshipEntity.RelationshipType type : LabUserRelationshipEntity.RelationshipType.values()) {
                if (type.getCode().equals(entity.getRelationshipType())) {
                    dto.setRelationshipTypeDesc(type.getDesc());
                    break;
                }
            }
        }

        if (entity.getStatus() != null) {
            for (LabUserRelationshipEntity.Status status : LabUserRelationshipEntity.Status.values()) {
                if (status.getCode().equals(entity.getStatus())) {
                    dto.setStatusDesc(status.getDesc());
                    break;
                }
            }
        }

        return dto;
    }
}
