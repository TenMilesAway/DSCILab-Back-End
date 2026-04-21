package com.agileboot.domain.lab.event;

import cn.hutool.core.util.StrUtil;
import com.agileboot.common.core.page.PageDTO;
import com.agileboot.common.exception.ApiException;
import com.agileboot.common.exception.error.ErrorCode;
import com.agileboot.domain.lab.event.author.LabEventAuthorEntity;
import com.agileboot.domain.lab.event.author.LabEventAuthorService;
import com.agileboot.domain.lab.event.command.CreateEventCommand;
import com.agileboot.domain.lab.event.command.UpdateEventCommand;
import com.agileboot.domain.lab.event.db.LabEventEntity;
import com.agileboot.domain.lab.event.db.LabEventService;
import com.agileboot.domain.lab.event.dto.LabEventAuthorDTO;
import com.agileboot.domain.lab.event.dto.LabEventDTO;
import com.agileboot.domain.lab.event.dto.LabEventListDTO;
import com.agileboot.domain.lab.event.query.LabEventQuery;
import com.agileboot.domain.lab.user.db.LabUserEntity;
import com.agileboot.domain.lab.user.db.LabUserService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class LabEventApplicationService {

    private static final int MAX_CONTENT_LENGTH = 50000;
    private static final int MAX_IMAGE_COUNT = 20;
    private static final Pattern IMG_SRC_PATTERN = Pattern.compile(
        "<img\\b[^>]*\\bsrc\\s*=\\s*['\\\"]([^'\\\"]+)['\\\"][^>]*>", Pattern.CASE_INSENSITIVE);
    private static final Pattern EVENT_HANDLER_PATTERN = Pattern.compile("\\son[a-z]+\\s*=", Pattern.CASE_INSENSITIVE);

    private final LabEventService eventService;
    private final LabEventAuthorService eventAuthorService;
    private final LabUserService labUserService;

    public PageDTO<LabEventListDTO> getEventList(LabEventQuery query) {
        QueryWrapper<LabEventEntity> wrapper = query.addQueryCondition();
        IPage<LabEventEntity> page = eventService.page(query.toPage(), wrapper);
        return buildEventPage(page);
    }

    public PageDTO<LabEventListDTO> getMyEventList(LabEventQuery query, Long currentUserId) {
        QueryWrapper<LabEventEntity> wrapper = query.addQueryCondition();
        applyEventAccessFilter(wrapper, currentUserId);
        IPage<LabEventEntity> page = eventService.page(query.toPage(), wrapper);
        return buildEventPage(page);
    }

    public PageDTO<LabEventListDTO> getPublicEventList(LabEventQuery query) {
        query.setPublished(true);
        return getEventList(query);
    }

    public LabEventDTO getEventDetail(Long id) {
        LabEventEntity entity = eventService.getById(id);
        if (entity == null || Boolean.TRUE.equals(entity.getDeleted())) {
            return null;
        }
        LabEventDTO dto = LabEventDTO.fromEntity(entity);
        fillAuthors(Collections.singletonList(dto));
        return dto;
    }

    @Transactional(rollbackFor = Exception.class)
    public Long createEvent(CreateEventCommand command, Long ownerUserId) {
        LabEventEntity entity = new LabEventEntity();
        fillEntity(entity, command, ownerUserId);
        entity.setCreateTime(new java.util.Date());
        entity.setUpdateTime(new java.util.Date());
        eventService.save(entity);
        saveEventAuthors(entity.getId(), command.getAuthors(), ownerUserId);
        log.info("event-create id={} title={} contentLength={}", entity.getId(), entity.getTitle(),
            safeLength(command.getContent()));
        return entity.getId();
    }

    @Transactional(rollbackFor = Exception.class)
    public void updateEvent(Long eventId, UpdateEventCommand command, Long operatorId, boolean isAdmin) {
        LabEventEntity entity = requireEvent(eventId);
        if (!canOperateEvent(entity, operatorId, isAdmin)) {
            throw new ApiException(ErrorCode.Business.PERMISSION_NOT_ALLOWED_TO_OPERATE);
        }
        fillEntity(entity, command, entity.getOwnerUserId());
        entity.setUpdateTime(new java.util.Date());
        eventService.updateById(entity);
        saveEventAuthors(eventId, command.getAuthors(), operatorId);
        log.info("event-update id={} title={} contentLength={}", eventId, command.getTitle(),
            safeLength(command.getContent()));
    }

    @Transactional(rollbackFor = Exception.class)
    public void deleteEvent(Long eventId, Long operatorId, boolean isAdmin) {
        LabEventEntity entity = requireEvent(eventId);
        if (!canOperateEvent(entity, operatorId, isAdmin)) {
            throw new ApiException(ErrorCode.Business.PERMISSION_NOT_ALLOWED_TO_OPERATE);
        }
        eventService.removeById(eventId);
    }

    @Transactional(rollbackFor = Exception.class)
    public void updatePublishStatus(Long eventId, Boolean published, Long operatorId, boolean isAdmin) {
        LabEventEntity entity = requireEvent(eventId);
        if (!canOperateEvent(entity, operatorId, isAdmin)) {
            throw new ApiException(ErrorCode.Business.PERMISSION_NOT_ALLOWED_TO_OPERATE);
        }
        entity.setPublished(Boolean.TRUE.equals(published));
        entity.setUpdateTime(new java.util.Date());
        eventService.updateById(entity);
    }

    private void fillEntity(LabEventEntity entity, CreateEventCommand command, Long ownerUserId) {
        validateRichContent(command.getContent());
        entity.setTitle(command.getTitle());
        entity.setSummary(command.getSummary());
        entity.setEventTime(command.getEventTime() == null ? LocalDate.now() : command.getEventTime());
        entity.setContent(command.getContent());
        entity.setTag(command.getTag());
        entity.setOwnerUserId(ownerUserId);
        entity.setPublished(Boolean.TRUE.equals(command.getPublished()));
        entity.setDeleted(false);
        entity.setCreatorId(ownerUserId);
        entity.setUpdaterId(ownerUserId);
    }

    private LabEventEntity requireEvent(Long eventId) {
        LabEventEntity entity = eventService.getById(eventId);
        if (entity == null || Boolean.TRUE.equals(entity.getDeleted())) {
            throw new ApiException(ErrorCode.Business.COMMON_OBJECT_NOT_FOUND, "", "活动");
        }
        return entity;
    }

    private void saveEventAuthors(Long eventId, List<CreateEventCommand.EventAuthorCommand> authors, Long operatorId) {
        eventAuthorService.lambdaUpdate()
            .eq(LabEventAuthorEntity::getEventId, eventId)
            .remove();
        if (authors == null || authors.isEmpty()) {
            return;
        }
        int fallbackOrder = 1;
        for (CreateEventCommand.EventAuthorCommand authorCmd : authors) {
            if (authorCmd == null) {
                continue;
            }
            if (authorCmd.getAuthorOrder() == null) {
                authorCmd.setAuthorOrder(fallbackOrder++);
            }
            if (authorCmd.getUserId() == null && StrUtil.isBlank(authorCmd.getName())) {
                throw new ApiException(ErrorCode.Client.COMMON_REQUEST_PARAMETERS_INVALID, "外部作者必须提供姓名");
            }
            LabUserEntity user = resolveUser(authorCmd);
            LabEventAuthorEntity entity = new LabEventAuthorEntity();
            entity.setEventId(eventId);
            if (user != null) {
                entity.setUserId(user.getId());
                entity.setName(StrUtil.isBlank(authorCmd.getName()) ? user.getRealName() : authorCmd.getName());
                entity.setNameEn(StrUtil.isBlank(authorCmd.getNameEn()) ? user.getEnglishName() : authorCmd.getNameEn());
                entity.setAffiliation(StrUtil.isBlank(authorCmd.getAffiliation()) ? user.getResearchArea() : authorCmd.getAffiliation());
            } else {
                entity.setUserId(null);
                entity.setName(authorCmd.getName());
                entity.setNameEn(authorCmd.getNameEn());
                entity.setAffiliation(authorCmd.getAffiliation());
            }
            entity.setAuthorOrder(authorCmd.getAuthorOrder());
            entity.setIsCorresponding(Boolean.TRUE.equals(authorCmd.getCorresponding()));
            entity.setRole(authorCmd.getRole());
            entity.setVisible(authorCmd.getVisible() == null ? true : authorCmd.getVisible());
            entity.setDeleted(false);
            entity.setCreateTime(new java.util.Date());
            entity.setUpdateTime(new java.util.Date());
            entity.setCreatorId(operatorId);
            entity.setUpdaterId(operatorId);
            eventAuthorService.save(entity);
        }
    }

    private LabUserEntity resolveUser(CreateEventCommand.EventAuthorCommand authorCmd) {
        if (authorCmd.getUserId() != null) {
            return labUserService.getById(authorCmd.getUserId());
        }
        if (StrUtil.isNotBlank(authorCmd.getName())) {
            LabUserEntity byCn = labUserService.findBestByRealName(authorCmd.getName().trim());
            if (byCn != null) {
                return byCn;
            }
        }
        if (StrUtil.isNotBlank(authorCmd.getNameEn())) {
            return labUserService.findBestByEnglishName(authorCmd.getNameEn().trim());
        }
        return null;
    }

    private boolean canOperateEvent(LabEventEntity entity, Long operatorId, boolean isAdmin) {
        if (isAdmin) {
            return true;
        }
        if (operatorId == null) {
            return false;
        }
        if (entity.getOwnerUserId() != null && entity.getOwnerUserId().equals(operatorId)) {
            return true;
        }
        return eventAuthorService.isAuthor(entity.getId(), operatorId);
    }

    private void fillAuthors(List<LabEventDTO> dtoList) {
        if (dtoList == null || dtoList.isEmpty()) {
            return;
        }
        List<Long> ids = dtoList.stream().map(LabEventDTO::getId).collect(Collectors.toList());
        Map<Long, List<LabEventAuthorDTO>> authorMap = eventAuthorService.getAuthorsByEventIds(ids)
            .stream()
            .collect(Collectors.groupingBy(
                LabEventAuthorEntity::getEventId,
                Collectors.mapping(LabEventAuthorDTO::fromEntity, Collectors.toList())
            ));
        dtoList.forEach(dto -> dto.setAuthors(authorMap.getOrDefault(dto.getId(), Collections.emptyList())));
    }

    private PageDTO<LabEventListDTO> buildEventPage(IPage<LabEventEntity> page) {
        List<LabEventListDTO> list = page.getRecords().stream()
            .map(LabEventListDTO::fromEntity)
            .collect(Collectors.toList());
        return new PageDTO<>(list, page.getTotal());
    }

    private void applyEventAccessFilter(QueryWrapper<LabEventEntity> wrapper, Long currentUserId) {
        if (currentUserId == null) {
            return;
        }
        List<Long> participantEventIds = eventAuthorService.lambdaQuery()
            .eq(LabEventAuthorEntity::getUserId, currentUserId)
            .eq(LabEventAuthorEntity::getDeleted, false)
            .list()
            .stream()
            .map(LabEventAuthorEntity::getEventId)
            .filter(Objects::nonNull)
            .distinct()
            .collect(Collectors.toList());
        if (participantEventIds.isEmpty()) {
            wrapper.eq("owner_user_id", currentUserId);
        } else {
            wrapper.and(w -> w.eq("owner_user_id", currentUserId)
                .or()
                .in("id", participantEventIds));
        }
    }

    private int safeLength(String content) {
        return content == null ? 0 : content.length();
    }

    private void validateRichContent(String content) {
        if (StrUtil.isBlank(content)) {
            return;
        }
        if (content.length() > MAX_CONTENT_LENGTH) {
            throw new ApiException(ErrorCode.Client.COMMON_REQUEST_PARAMETERS_INVALID,
                "content长度不能超过" + MAX_CONTENT_LENGTH);
        }
        String lower = content.toLowerCase();
        if (lower.contains("data:image/")) {
            throw new ApiException(ErrorCode.Client.COMMON_REQUEST_PARAMETERS_INVALID,
                "content中禁止使用base64图片，请先上传后引用URL");
        }
        if (lower.contains("<script") || lower.contains("javascript:")) {
            throw new ApiException(ErrorCode.Client.COMMON_REQUEST_PARAMETERS_INVALID,
                "content包含不安全脚本内容");
        }
        if (EVENT_HANDLER_PATTERN.matcher(content).find()) {
            throw new ApiException(ErrorCode.Client.COMMON_REQUEST_PARAMETERS_INVALID,
                "content包含不安全事件属性");
        }
        if (lower.contains("<iframe") || lower.contains("<object") || lower.contains("<embed")) {
            throw new ApiException(ErrorCode.Client.COMMON_REQUEST_PARAMETERS_INVALID,
                "content包含不安全标签");
        }

        Matcher matcher = IMG_SRC_PATTERN.matcher(content);
        int imgCount = 0;
        while (matcher.find()) {
            imgCount++;
            if (imgCount > MAX_IMAGE_COUNT) {
                throw new ApiException(ErrorCode.Client.COMMON_REQUEST_PARAMETERS_INVALID,
                    "content图片数量不能超过" + MAX_IMAGE_COUNT);
            }
            String src = StrUtil.trim(matcher.group(1));
            if (StrUtil.isBlank(src)) {
                throw new ApiException(ErrorCode.Client.COMMON_REQUEST_PARAMETERS_INVALID,
                    "content中存在空图片地址");
            }
            String srcLower = src.toLowerCase();
            if (!(srcLower.startsWith("http://") || srcLower.startsWith("https://") || srcLower.startsWith("/"))) {
                throw new ApiException(ErrorCode.Client.COMMON_REQUEST_PARAMETERS_INVALID,
                    "content图片地址仅支持http/https或站内相对路径");
            }
            if (srcLower.startsWith("data:")) {
                throw new ApiException(ErrorCode.Client.COMMON_REQUEST_PARAMETERS_INVALID,
                    "content中禁止使用data协议图片");
            }
        }
    }
}
