package com.agileboot.admin.controller.lab;

import com.agileboot.common.core.page.PageDTO;
import com.agileboot.domain.lab.achievement.LabAchievementApplicationService;
import com.agileboot.domain.lab.achievement.dto.LabAchievementDTO;
import com.agileboot.domain.lab.user.LabUserPermissionChecker;
import com.agileboot.domain.lab.user.db.LabUserEntity;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.Collections;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(LabAchievementController.class)
class LabAchievementControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private LabAchievementApplicationService achievementApplicationService;

    @MockBean
    private LabUserPermissionChecker labUserPermissionChecker;

    private LabUserEntity mockUser;
    private LabAchievementDTO mockAchievementDTO;

    @BeforeEach
    void setUp() {
        mockUser = new LabUserEntity();
        mockUser.setId(100L);
        mockUser.setRealName("Test User");
        mockUser.setIdentity(1); // 假设1为管理员

        mockAchievementDTO = new LabAchievementDTO();
        mockAchievementDTO.setId(1L);
        mockAchievementDTO.setTitle("Test Paper");
        mockAchievementDTO.setType(1);
        mockAchievementDTO.setPaperType(1);
        mockAchievementDTO.setPublishDate(LocalDate.of(2024, 6, 1));
        mockAchievementDTO.setOwnerUserId(100L);
        mockAchievementDTO.setPublished(false);
        mockAchievementDTO.setIsVerified(false);
    }

    @Test
    @WithMockUser(authorities = "lab:achievement:list")
    void testList_Success() throws Exception {
        // Given
        PageDTO<LabAchievementDTO> pageDTO = new PageDTO<>(
            Collections.singletonList(mockAchievementDTO), 1L);
        when(achievementApplicationService.getAchievementList(any())).thenReturn(pageDTO);

        // When & Then
        mockMvc.perform(get("/lab/achievements")
                .param("pageNum", "1")
                .param("pageSize", "10")
                .param("type", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.total").value(1))
                .andExpect(jsonPath("$.data.list[0].title").value("Test Paper"));
    }

    @Test
    @WithMockUser(authorities = "lab:achievement:query")
    void testGetDetail_Success() throws Exception {
        // Given
        when(achievementApplicationService.getAchievementDetail(1L)).thenReturn(mockAchievementDTO);

        // When & Then
        mockMvc.perform(get("/lab/achievements/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpected(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.title").value("Test Paper"));
    }

    @Test
    @WithMockUser(authorities = "lab:achievement:add")
    void testCreate_Success() throws Exception {
        // Given
        when(labUserPermissionChecker.getCurrentLabUser()).thenReturn(mockUser);
        when(achievementApplicationService.createAchievement(any(), eq(100L))).thenReturn(1L);

        String requestBody = """
            {
                "title": "Test Paper",
                "type": 1,
                "paperType": 1,
                "venue": "Test Journal",
                "publishDate": "2024-06-01",
                "published": false
            }
            """;

        // When & Then
        mockMvc.perform(post("/lab/achievements")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data").value(1));
    }

    @Test
    @WithMockUser(authorities = "lab:achievement:add")
    void testCreate_WithAuthors_Success() throws Exception {
        // Given
        when(labUserPermissionChecker.getCurrentLabUser()).thenReturn(mockUser);
        when(achievementApplicationService.createAchievement(any(), eq(100L))).thenReturn(1L);

        String requestBody = """
            {
                "title": "Test Paper With Authors",
                "type": 1,
                "paperType": 1,
                "venue": "Test Journal",
                "publishDate": "2024-06-01",
                "published": false,
                "authors": [
                    {
                        "userId": 5,
                        "authorOrder": 1,
                        "isCorresponding": false,
                        "role": "第一作者",
                        "visible": true
                    },
                    {
                        "name": "John Doe",
                        "affiliation": "MIT",
                        "authorOrder": 2,
                        "isCorresponding": true,
                        "role": "通讯作者",
                        "visible": true
                    }
                ]
            }
            """;

        // When & Then
        mockMvc.perform(post("/lab/achievements")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data").value(1));
    }

    @Test
    @WithMockUser(authorities = "lab:achievement:edit")
    void testUpdate_Success() throws Exception {
        // Given
        when(labUserPermissionChecker.getCurrentLabUser()).thenReturn(mockUser);
        when(labUserPermissionChecker.isAdmin()).thenReturn(true);

        String requestBody = """
            {
                "title": "Updated Test Paper",
                "type": 1,
                "paperType": 2,
                "venue": "Updated Journal",
                "publishDate": "2024-07-01",
                "published": false
            }
            """;

        // When & Then
        mockMvc.perform(put("/lab/achievements/1")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0));
    }

    @Test
    @WithMockUser(authorities = "lab:achievement:publish")
    void testPublish_Success() throws Exception {
        // Given
        when(labUserPermissionChecker.getCurrentLabUser()).thenReturn(mockUser);
        when(labUserPermissionChecker.isAdmin()).thenReturn(true);

        // When & Then
        mockMvc.perform(put("/lab/achievements/1/publish")
                .with(csrf())
                .param("published", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0));
    }

    @Test
    @WithMockUser(authorities = "lab:achievement:verify")
    void testVerify_Success() throws Exception {
        // Given
        when(labUserPermissionChecker.getCurrentLabUser()).thenReturn(mockUser);
        when(labUserPermissionChecker.isAdmin()).thenReturn(true);

        // When & Then
        mockMvc.perform(put("/lab/achievements/1/verify")
                .with(csrf())
                .param("verified", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0));
    }

    @Test
    @WithMockUser(authorities = "lab:achievement:remove")
    void testDelete_Success() throws Exception {
        // Given
        when(labUserPermissionChecker.getCurrentLabUser()).thenReturn(mockUser);
        when(labUserPermissionChecker.isAdmin()).thenReturn(true);

        // When & Then
        mockMvc.perform(delete("/lab/achievements/1")
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0));
    }

    @Test
    void testList_WithoutPermission_Forbidden() throws Exception {
        // When & Then
        mockMvc.perform(get("/lab/achievements"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(authorities = "lab:achievement:add")
    void testCreate_InvalidRequest_BadRequest() throws Exception {
        // Given
        when(labUserPermissionChecker.getCurrentLabUser()).thenReturn(mockUser);

        String invalidRequestBody = """
            {
                "title": "",
                "type": 1,
                "paperType": 1,
                "publishDate": "2024-06-01"
            }
            """;

        // When & Then
        mockMvc.perform(post("/lab/achievements")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(invalidRequestBody))
                .andExpect(status().isBadRequest());
    }
}
