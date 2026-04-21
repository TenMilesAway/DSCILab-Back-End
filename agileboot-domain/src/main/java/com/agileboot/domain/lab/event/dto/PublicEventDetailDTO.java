/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  io.swagger.v3.oas.annotations.media.Schema
 *  lombok.Generated
 */
package com.agileboot.domain.lab.event.dto;

import com.agileboot.domain.lab.event.dto.LabEventDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDate;
import lombok.Generated;

@Schema(description="\u516c\u5f00\u6d3b\u52a8\u8be6\u60c5")
public class PublicEventDetailDTO {
    private Long id;
    private String title;
    private String summary;
    private LocalDate eventTime;
    private String content;
    private String tag;

    public static PublicEventDetailDTO fromLabDetail(LabEventDTO dto) {
        PublicEventDetailDTO detail = new PublicEventDetailDTO();
        detail.setId(dto.getId());
        detail.setTitle(dto.getTitle());
        detail.setSummary(dto.getSummary());
        detail.setEventTime(dto.getEventTime());
        detail.setContent(dto.getContent());
        detail.setTag(dto.getTag());
        return detail;
    }

    @Generated
    public PublicEventDetailDTO() {
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
    public String getContent() {
        return this.content;
    }

    @Generated
    public String getTag() {
        return this.tag;
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
    public void setContent(String content) {
        this.content = content;
    }

    @Generated
    public void setTag(String tag) {
        this.tag = tag;
    }

    @Generated
    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }
        if (!(o instanceof PublicEventDetailDTO)) {
            return false;
        }
        PublicEventDetailDTO other = (PublicEventDetailDTO)o;
        if (!other.canEqual(this)) {
            return false;
        }
        Long this$id = this.getId();
        Long other$id = other.getId();
        if (this$id == null ? other$id != null : !((Object)this$id).equals(other$id)) {
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
        if (this$eventTime == null ? other$eventTime != null : !((Object)this$eventTime).equals(other$eventTime)) {
            return false;
        }
        String this$content = this.getContent();
        String other$content = other.getContent();
        if (this$content == null ? other$content != null : !this$content.equals(other$content)) {
            return false;
        }
        String this$tag = this.getTag();
        String other$tag = other.getTag();
        return !(this$tag == null ? other$tag != null : !this$tag.equals(other$tag));
    }

    @Generated
    protected boolean canEqual(Object other) {
        return other instanceof PublicEventDetailDTO;
    }

    @Generated
    public int hashCode() {
        int PRIME = 59;
        int result = 1;
        Long $id = this.getId();
        result = result * 59 + ($id == null ? 43 : ((Object)$id).hashCode());
        String $title = this.getTitle();
        result = result * 59 + ($title == null ? 43 : $title.hashCode());
        String $summary = this.getSummary();
        result = result * 59 + ($summary == null ? 43 : $summary.hashCode());
        LocalDate $eventTime = this.getEventTime();
        result = result * 59 + ($eventTime == null ? 43 : ((Object)$eventTime).hashCode());
        String $content = this.getContent();
        result = result * 59 + ($content == null ? 43 : $content.hashCode());
        String $tag = this.getTag();
        result = result * 59 + ($tag == null ? 43 : $tag.hashCode());
        return result;
    }

    @Generated
    public String toString() {
        return "PublicEventDetailDTO(id=" + this.getId() + ", title=" + this.getTitle() + ", summary=" + this.getSummary() + ", eventTime=" + this.getEventTime() + ", content=" + this.getContent() + ", tag=" + this.getTag() + ")";
    }
}
