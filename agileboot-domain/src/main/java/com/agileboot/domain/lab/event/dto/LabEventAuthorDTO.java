package com.agileboot.domain.lab.event.dto;

import com.agileboot.domain.lab.event.author.LabEventAuthorEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "活动作者")
public class LabEventAuthorDTO {

    private Long id;
    private Long eventId;
    private Long userId;
    private String name;
    private String nameEn;
    private String affiliation;
    private Integer authorOrder;
    private Boolean isCorresponding;
    private String role;
    private Boolean visible;
    private Boolean isInternal;

    public static LabEventAuthorDTO fromEntity(LabEventAuthorEntity entity) {
        if (entity == null) {
            return null;
        }
        LabEventAuthorDTO dto = new LabEventAuthorDTO();
        dto.setId(entity.getId());
        dto.setEventId(entity.getEventId());
        dto.setUserId(entity.getUserId());
        dto.setName(entity.getName());
        dto.setNameEn(entity.getNameEn());
        dto.setAffiliation(entity.getAffiliation());
        dto.setAuthorOrder(entity.getAuthorOrder());
        dto.setIsCorresponding(Boolean.TRUE.equals(entity.getIsCorresponding()));
        dto.setRole(entity.getRole());
        dto.setVisible(Boolean.TRUE.equals(entity.getVisible()));
        dto.setIsInternal(entity.getUserId() != null);
        return dto;
    }
}
