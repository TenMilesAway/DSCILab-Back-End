package com.agileboot.domain.lab.achievement;

import com.agileboot.common.core.page.PageDTO;
import com.agileboot.common.exception.ApiException;
import com.agileboot.domain.lab.achievement.command.CreateLabAchievementCommand;
import com.agileboot.domain.lab.achievement.command.CreateAuthorCommand;
import com.agileboot.domain.lab.achievement.db.LabAchievementEntity;
import com.agileboot.domain.lab.achievement.db.LabAchievementService;
import com.agileboot.domain.lab.achievement.db.LabAchievementAuthorEntity;
import com.agileboot.domain.lab.achievement.db.LabAchievementAuthorService;
import com.agileboot.domain.lab.achievement.dto.LabAchievementDTO;
import com.agileboot.domain.lab.achievement.dto.PublicAchievementDTO;
import com.agileboot.domain.lab.achievement.query.LabAchievementQuery;
import com.agileboot.domain.lab.achievement.query.PublicAchievementQuery;
import com.agileboot.domain.lab.user.db.LabUserService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LabAchievementApplicationServiceTest {

    @Mock
    private LabAchievementService achievementService;

    @Mock
    private LabAchievementAuthorService authorService;

    @Mock
    private LabUserService labUserService;

    @InjectMocks
    private LabAchievementApplicationService applicationService;

    private LabAchievementEntity mockAchievement;
    private CreateLabAchievementCommand mockCommand;

    @BeforeEach
    void setUp() {
        mockAchievement = new LabAchievementEntity();
        mockAchievement.setId(1L);
        mockAchievement.setTitle("Test Paper");
        mockAchievement.setType(1);
        mockAchievement.setPaperType(1);
        mockAchievement.setPublishDate(LocalDate.of(2024, 6, 1));
        mockAchievement.setOwnerUserId(100L);
        mockAchievement.setPublished(false);
        mockAchievement.setIsVerified(false);
        mockAchievement.setDeleted(false);

        mockCommand = new CreateLabAchievementCommand();
        mockCommand.setTitle("Test Paper");
        mockCommand.setType(1);
        mockCommand.setPaperType(1);
        mockCommand.setPublishDate(LocalDate.of(2024, 6, 1));
        mockCommand.setPublished(false);
    }

    @Test
    void testCreateAchievement_Success() {
        // Given
        when(achievementService.save(any(LabAchievementEntity.class))).thenReturn(true);
        doAnswer(invocation -> {
            LabAchievementEntity entity = invocation.getArgument(0);
            entity.setId(1L);
            return null;
        }).when(achievementService).save(any(LabAchievementEntity.class));

        // When
        Long result = applicationService.createAchievement(mockCommand, 100L);

        // Then
        assertNotNull(result);
        assertEquals(1L, result);
        verify(achievementService).save(any(LabAchievementEntity.class));
    }

    @Test
    void testCreateAchievement_WithAuthors_Success() {
        // Given
        CreateAuthorCommand author1 = new CreateAuthorCommand();
        author1.setUserId(5L);
        author1.setAuthorOrder(1);
        author1.setIsCorresponding(false);
        author1.setVisible(true);

        CreateAuthorCommand author2 = new CreateAuthorCommand();
        author2.setName("John Doe");
        author2.setAuthorOrder(2);
        author2.setIsCorresponding(true);
        author2.setVisible(true);

        mockCommand.setAuthors(Arrays.asList(author1, author2));

        when(achievementService.save(any(LabAchievementEntity.class))).thenReturn(true);
        doAnswer(invocation -> {
            LabAchievementEntity entity = invocation.getArgument(0);
            entity.setId(1L);
            return null;
        }).when(achievementService).save(any(LabAchievementEntity.class));
        when(authorService.saveBatch(anyList())).thenReturn(true);

        // When
        Long result = applicationService.createAchievement(mockCommand, 100L);

        // Then
        assertNotNull(result);
        assertEquals(1L, result);
        verify(achievementService).save(any(LabAchievementEntity.class));
        verify(authorService).saveBatch(argThat(list -> list.size() == 2));
    }

    @Test
    void testCreateAchievement_WithDuplicateAuthorOrder_ThrowsException() {
        // Given
        CreateAuthorCommand author1 = new CreateAuthorCommand();
        author1.setUserId(5L);
        author1.setAuthorOrder(1);

        CreateAuthorCommand author2 = new CreateAuthorCommand();
        author2.setName("John Doe");
        author2.setAuthorOrder(1); // 重复顺序

        mockCommand.setAuthors(Arrays.asList(author1, author2));

        when(achievementService.save(any(LabAchievementEntity.class))).thenReturn(true);
        doAnswer(invocation -> {
            LabAchievementEntity entity = invocation.getArgument(0);
            entity.setId(1L);
            return null;
        }).when(achievementService).save(any(LabAchievementEntity.class));

        // When & Then
        ApiException exception = assertThrows(ApiException.class, 
            () -> applicationService.createAchievement(mockCommand, 100L));
        assertTrue(exception.getMessage().contains("作者顺序重复"));
    }

    @Test
    void testGetPublicAchievementList_OnlyPublishedAndVerified() {
        // Given
        PublicAchievementQuery query = new PublicAchievementQuery();
        query.setPageNum(1);
        query.setPageSize(10);

        LabAchievementEntity publicAchievement = new LabAchievementEntity();
        publicAchievement.setId(1L);
        publicAchievement.setTitle("Public Paper");
        publicAchievement.setPublished(true);
        publicAchievement.setIsVerified(true);
        publicAchievement.setDeleted(false);

        IPage<LabAchievementEntity> mockPage = new Page<>(1, 10);
        mockPage.setRecords(Collections.singletonList(publicAchievement));
        mockPage.setTotal(1);

        when(achievementService.page(any(IPage.class), any(LambdaQueryWrapper.class)))
            .thenReturn(mockPage);

        // When
        PageDTO<PublicAchievementDTO> result = applicationService.getPublicAchievementList(query);

        // Then
        assertNotNull(result);
        assertEquals(1, result.getTotal());
        assertEquals(1, result.getList().size());
        assertEquals("Public Paper", result.getList().get(0).getTitle());

        verify(achievementService).page(any(IPage.class), 
            argThat(wrapper -> {
                // 验证查询条件包含 published=true 和 is_verified=true
                return wrapper.toString().contains("published") && 
                       wrapper.toString().contains("is_verified");
            }));
    }

    @Test
    void testDeleteAchievement_CascadeDeleteAuthors() {
        // Given
        Long achievementId = 1L;
        Long currentUserId = 100L;
        boolean isAdmin = true;

        when(achievementService.getByIdNotDeleted(achievementId)).thenReturn(mockAchievement);
        when(achievementService.updateById(any(LabAchievementEntity.class))).thenReturn(true);
        when(authorService.lambdaUpdate()).thenReturn(mock(com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper.class));

        // When
        applicationService.deleteAchievement(achievementId, currentUserId, isAdmin);

        // Then
        verify(achievementService).updateById(argThat(entity -> entity.getDeleted()));
        verify(authorService).lambdaUpdate();
    }

    @Test
    void testToggleMyVisibilityInAchievement_Success() {
        // Given
        Long achievementId = 1L;
        Long currentUserId = 5L;
        Boolean visible = true;

        LabAchievementAuthorEntity authorEntity = new LabAchievementAuthorEntity();
        authorEntity.setId(10L);
        authorEntity.setAchievementId(achievementId);
        authorEntity.setUserId(currentUserId);
        authorEntity.setVisible(false);

        when(achievementService.getByIdNotDeleted(achievementId)).thenReturn(mockAchievement);
        when(authorService.getAuthorRecord(achievementId, currentUserId)).thenReturn(authorEntity);
        when(authorService.updateById(any(LabAchievementAuthorEntity.class))).thenReturn(true);

        // When
        applicationService.toggleMyVisibilityInAchievement(achievementId, visible, currentUserId);

        // Then
        verify(authorService).updateById(argThat(entity -> entity.getVisible()));
    }

    @Test
    void testToggleMyVisibilityInAchievement_NotAuthor_ThrowsException() {
        // Given
        Long achievementId = 1L;
        Long currentUserId = 5L;
        Boolean visible = true;

        when(achievementService.getByIdNotDeleted(achievementId)).thenReturn(mockAchievement);
        when(authorService.getAuthorRecord(achievementId, currentUserId)).thenReturn(null);

        // When & Then
        ApiException exception = assertThrows(ApiException.class, 
            () -> applicationService.toggleMyVisibilityInAchievement(achievementId, visible, currentUserId));
        assertTrue(exception.getMessage().contains("您不是该成果的作者"));
    }
}
