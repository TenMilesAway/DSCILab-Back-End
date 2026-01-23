package com.agileboot.domain.lab.paper.dto;

import com.agileboot.domain.lab.paper.author.LabPaperAuthorEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "论文作者（公开视图）")
public class PublicPaperAuthorDTO {

    private String name;
    private String nameEn;
    private String affiliation;
    private Boolean corresponding;
    private Integer order;

    public static PublicPaperAuthorDTO fromEntity(LabPaperAuthorEntity entity) {
        if (entity == null) {
            return null;
        }
        PublicPaperAuthorDTO dto = new PublicPaperAuthorDTO();
        dto.setName(entity.getName());
        dto.setNameEn(entity.getNameEn());
        dto.setAffiliation(entity.getAffiliation());
        dto.setCorresponding(Boolean.TRUE.equals(entity.getIsCorresponding()));
        dto.setOrder(entity.getAuthorOrder());
        return dto;
    }
}
