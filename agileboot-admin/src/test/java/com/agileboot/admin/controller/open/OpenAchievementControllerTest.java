package com.agileboot.admin.controller.open;

import com.agileboot.common.core.page.PageDTO;
import com.agileboot.domain.lab.achievement.LabAchievementApplicationService;
import com.agileboot.domain.lab.achievement.dto.PublicAchievementDTO;
import com.agileboot.domain.lab.achievement.dto.PublicAuthorDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(OpenAchievementController.class)
class OpenAchievementControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private LabAchievementApplicationService achievementApplicationService;

    private PublicAchievementDTO mockPublicAchievementDTO;

    @BeforeEach
    void setUp() {
        mockPublicAchievementDTO = new PublicAchievementDTO();
        mockPublicAchievementDTO.setId(1L);
        mockPublicAchievementDTO.setTitle("Public Test Paper");
        mockPublicAchievementDTO.setType(1);
        mockPublicAchievementDTO.setTypeDesc("论文");
        mockPublicAchievementDTO.setPaperType(1);
        mockPublicAchievementDTO.setPaperTypeDesc("期刊论文");
        mockPublicAchievementDTO.setVenue("Test Journal");
        mockPublicAchievementDTO.setPublishDate(LocalDate.of(2024, 6, 1));
        mockPublicAchievementDTO.setCreateTime(new Date());

        // 设置作者列表（已过滤可见性）
        PublicAuthorDTO author1 = new PublicAuthorDTO();
        author1.setName("张三");
        author1.setAuthorOrder(1);
        author1.setIsCorresponding(false);
        author1.setRole("第一作者");

        PublicAuthorDTO author2 = new PublicAuthorDTO();
        author2.setName("John Doe");
        author2.setNameEn("John Doe");
        author2.setAffiliation("MIT");
        author2.setAuthorOrder(2);
        author2.setIsCorresponding(true);
        author2.setRole("通讯作者");

        mockPublicAchievementDTO.setAuthors(Arrays.asList(author1, author2));
    }

    @Test
    void testList_Success() throws Exception {
        // Given
        PageDTO<PublicAchievementDTO> pageDTO = new PageDTO<>(
            Collections.singletonList(mockPublicAchievementDTO), 1L);
        when(achievementApplicationService.getPublicAchievementList(any())).thenReturn(pageDTO);

        // When & Then
        mockMvc.perform(get("/open/achievements")
                .param("pageNum", "1")
                .param("pageSize", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.total").value(1))
                .andExpect(jsonPath("$.data.list[0].title").value("Public Test Paper"))
                .andExpect(jsonPath("$.data.list[0].typeDesc").value("论文"));
    }

    @Test
    void testGetDetail_Success() throws Exception {
        // Given
        when(achievementApplicationService.getPublicAchievementDetail(1L))
            .thenReturn(mockPublicAchievementDTO);

        // When & Then
        mockMvc.perform(get("/open/achievements/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.title").value("Public Test Paper"))
                .andExpect(jsonPath("$.data.authors").isArray())
                .andExpect(jsonPath("$.data.authors[0].name").value("张三"))
                .andExpect(jsonPath("$.data.authors[1].affiliation").value("MIT"));
    }

    @Test
    void testList_WithFilters_Success() throws Exception {
        // Given
        PageDTO<PublicAchievementDTO> pageDTO = new PageDTO<>(
            Collections.singletonList(mockPublicAchievementDTO), 1L);
        when(achievementApplicationService.getPublicAchievementList(any())).thenReturn(pageDTO);

        // When & Then
        mockMvc.perform(get("/open/achievements")
                .param("type", "1")
                .param("keyword", "AI")
                .param("dateStart", "2024-01-01")
                .param("dateEnd", "2024-12-31"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.total").value(1));
    }
}
