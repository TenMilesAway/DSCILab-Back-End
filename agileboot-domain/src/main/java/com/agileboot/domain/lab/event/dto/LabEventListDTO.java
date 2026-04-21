/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  io.swagger.v3.oas.annotations.media.Schema
 *  lombok.Generated
 */
package com.agileboot.domain.lab.event.dto;

import com.agileboot.domain.lab.event.db.LabEventEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDate;
import lombok.Generated;

@Schema(description="\u6d3b\u52a8\u5217\u8868\u9879")
public class LabEventListDTO {
    private Long id;
    private String title;
    private String summary;
    private LocalDate eventTime;
    private Boolean published;

    public static LabEventListDTO fromEntity(LabEventEntity entity) {
        LabEventListDTO dto = new LabEventListDTO();
        dto.setId(entity.getId());
        dto.setTitle(entity.getTitle());
        dto.setSummary(entity.getSummary());
        dto.setEventTime(entity.getEventTime());
        dto.setPublished(entity.getPublished());
        return dto;
    }

    @Generated
    public LabEventListDTO() {
    }

    @Generated
    public Long getId() {
        return this.id;
    }

    @Generated
    public String getTitle() {
        return this.title;
    }

    @Generated
    public String getSummary() {
        return this.summary;
    }

    @Generated
    public LocalDate getEventTime() {
        return this.eventTime;
    }

    @Generated
    public Boolean getPublished() {
        return this.published;
    }

    @Generated
    public void setId(Long id) {
        this.id = id;
    }

    @Generated
    public void setTitle(String title) {
        this.title = title;
    }

    @Generated
    public void setSummary(String summary) {
        this.summary = summary;
    }

    @Generated
    public void setEventTime(LocalDate eventTime) {
        this.eventTime = eventTime;
    }

    @Generated
    public void setPublished(Boolean published) {
        this.published = published;
    }

    @Generated
    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }
        if (!(o instanceof LabEventListDTO)) {
            return false;
        }
        LabEventListDTO other = (LabEventListDTO)o;
        if (!other.canEqual(this)) {
            return false;
        }
        Long this$id = this.getId();
        Long other$id = other.getId();
        if (this$id == null ? other$id != null : !((Object)this$id).equals(other$id)) {
            return false;
        }
        Boolean this$published = this.getPublished();
        Boolean other$published = other.getPublished();
        if (this$published == null ? other$published != null : !((Object)this$published).equals(other$published)) {
            return false;
        }
        String this$title = this.getTitle();
        String other$title = other.getTitle();
        if (this$title == null ? other$title != null : !this$title.equals(other$title)) {
            return false;
        }
        String this$summary = this.getSummary();
        String other$summary = other.getSummary();
        if (this$summary == null ? other$summary != null : !this$summary.equals(other$summary)) {
            return false;
        }
        LocalDate this$eventTime = this.getEventTime();
        LocalDate other$eventTime = other.getEventTime();
        return !(this$eventTime == null ? other$eventTime != null : !((Object)this$eventTime).equals(other$eventTime));
    }

    @Generated
    protected boolean canEqual(Object other) {
        return other instanceof LabEventListDTO;
    }

    @Generated
    public int hashCode() {
        int PRIME = 59;
        int result = 1;
        Long $id = this.getId();
        result = result * 59 + ($id == null ? 43 : ((Object)$id).hashCode());
        Boolean $published = this.getPublished();
        result = result * 59 + ($published == null ? 43 : ((Object)$published).hashCode());
        String $title = this.getTitle();
        result = result * 59 + ($title == null ? 43 : $title.hashCode());
        String $summary = this.getSummary();
        result = result * 59 + ($summary == null ? 43 : $summary.hashCode());
        LocalDate $eventTime = this.getEventTime();
        result = result * 59 + ($eventTime == null ? 43 : ((Object)$eventTime).hashCode());
        return result;
    }

    @Generated
    public String toString() {
        return "LabEventListDTO(id=" + this.getId() + ", title=" + this.getTitle() + ", summary=" + this.getSummary() + ", eventTime=" + this.getEventTime() + ", published=" + this.getPublished() + ")";
    }
}
