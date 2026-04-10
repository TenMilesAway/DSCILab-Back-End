package com.agileboot.domain.lab.event.dto;

import com.agileboot.domain.lab.event.db.LabEventEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDate;
import java.util.Collections;
import java.util.Date;
import java.util.List;

@Data
@Schema(description = "活动详情")
public class LabEventDTO {

    private Long id;
    private String title;
    private String summary;
    private LocalDate eventTime;
    private String content;
    private String tag;
    private Long ownerUserId;
    private Boolean published;
    private Date createTime;
    private Date updateTime;
    private List<LabEventAuthorDTO> authors = Collections.emptyList();

    public static LabEventDTO fromEntity(LabEventEntity entity) {
        LabEventDTO dto = new LabEventDTO();
        dto.setId(entity.getId());
        dto.setTitle(entity.getTitle());
        dto.setSummary(entity.getSummary());
        dto.setEventTime(entity.getEventTime());
        dto.setContent(entity.getContent());
        dto.setTag(entity.getTag());
        dto.setOwnerUserId(entity.getOwnerUserId());
        dto.setPublished(entity.getPublished());
        dto.setCreateTime(entity.getCreateTime());
        dto.setUpdateTime(entity.getUpdateTime());
        dto.setAuthors(Collections.emptyList());
        return dto;
    }
}
